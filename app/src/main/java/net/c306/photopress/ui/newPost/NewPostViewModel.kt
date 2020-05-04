package net.c306.photopress.ui.newPost

import android.app.Application
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.c306.photopress.api.*
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class NewPostViewModel(application: Application) : AndroidViewModel(application) {
    
    private val applicationContext = application.applicationContext
    
    
    // Fragment state
    enum class State {
        /** Image not selected. Can't publish. */
        EMPTY,
        /** Image selected, title text not available. Can't publish. */
        HAVE_IMAGE,
        /** Image & title text are both available. Can publish. */
        READY,
        /** Publishing post. */
        PUBLISHING
    }
    
    private val _state = MutableLiveData<State>().apply { value = State.EMPTY }
    val state: LiveData<State> = _state
    
    private fun updateState() {
        val title = titleText.value ?: ""
        val image = imageUri.value
        
        _state.value = when {
            image == null -> State.EMPTY
            title.isBlank() -> State.HAVE_IMAGE
            else -> State.READY
        }
    }
    
    
    
    // Image Uri
    private val _imageUri = MutableLiveData<Uri>()
    val imageUri: LiveData<Uri> = _imageUri
    
    fun setImageUri(value: Uri?) {
        _imageUri.value = value
        updateState()
    }
    
    
    data class FileDetails(
        val fileName: String,
        val mimeType: String
    )
    
    val fileDetails = Transformations.switchMap(imageUri) {
        MutableLiveData<FileDetails?>().apply {
            value = if (it == null) null else getFileName(it)
        }
    }
    
    
    
    // Title text
    private val _titleText = MutableLiveData<String>()
    val titleText: LiveData<String> = _titleText
    
    fun setTitleText(value: String?) {
        _titleText.value = value
        updateState()
    }
    
    data class PublishedPost(
        val post: WPBlogPost,
        val isDraft: Boolean
    )
    
    // Published post data
    private val _publishedPost = MutableLiveData<PublishedPost>()
    val publishedPost = _publishedPost
    
    
    
    init {
        updateState()
    }
    
    
    
    private fun getFileForUri(uri: Uri): Pair<File, String>? {
        
        val fileDetails =  getFileName(uri)
        
        // Open a specific media item using ParcelFileDescriptor.
        val resolver = applicationContext.contentResolver
        
        // Open selected file as input stream
        resolver.openInputStream(uri)?.use { stream ->
            // Write it to app's storage as file
            val imageFile = File(applicationContext.filesDir, fileDetails.fileName)
            imageFile.outputStream().use {
                stream.copyTo(it)
            }
            return Pair(imageFile, fileDetails.mimeType)
        }
        
        return null
    }
    
    
    private fun getFileName(uri: Uri): FileDetails {
        val cr = applicationContext.contentResolver
        val projection = arrayOf(
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.MIME_TYPE
        )
        val metaCursor = cr.query(uri, projection, null, null, null)
        
        var fileName = ""
        var mimeType = ""
        metaCursor?.use { it ->
            if (it.moveToFirst()) {
                fileName = it.getString(0)
                mimeType = it.getString(1)
            }
        }
        
        return FileDetails(fileName, mimeType)
    }
    
    
    internal fun publishPost(
        blogId: Int,
        blogTitle: String,
        imageUri: Uri
    ) {
        
        val imageDetails = getFileForUri(imageUri)
        
        if (imageDetails == null) {
            Timber.w("File not found!: $imageUri")
            return
        }
        
        // Reset published post data
        _publishedPost.value = null
        _state.value = State.PUBLISHING
        
        val (file, mimeType) = imageDetails
        
        viewModelScope.launch {
            
            // Upload media to WP
            val (media, mediaError) = uploadMedia(
                blogId,
                file,
                mimeType.toMediaType()
            )
            
            if (mediaError != null || media == null) {
                // Show error message and reset state
                withContext(Dispatchers.Main) {
                    Toast.makeText(applicationContext, mediaError ?: "No media returned", Toast.LENGTH_LONG).show()
                    _state.value = State.READY
                }
                return@launch
            }
            
            // Upload post as draft with embedded image
            val (uploadedPost, uploadError) = uploadPost(
                blogId,
                media,
                blogTitle
            )
            
            if (uploadError != null || uploadedPost == null) {
                // Show error message and reset state
                withContext(Dispatchers.Main) {
                    Toast.makeText(applicationContext, uploadError ?: "Uploaded post not returned", Toast.LENGTH_LONG).show()
                    _state.value = State.READY
                }
                // TODO("Maybe delete uploaded media, or store reference to it to retry.")
                return@launch
            }
            
            
            
            // Change status to published
            val (publishedPost, publishError) = updateToPublished(
                blogId,
                uploadedPost
            )
    
            if (publishError != null || publishedPost == null) {
                Toast.makeText(applicationContext, "Post uploaded as draft.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(applicationContext, "Post published successfully.", Toast.LENGTH_SHORT).show()
            }
            
            
            Timber.d("Blogpost done! ${publishedPost ?: uploadedPost}")
            
            // Update published post
            _publishedPost.value = publishedPost
                ?.let { PublishedPost(it, false) }
                ?: PublishedPost(uploadedPost, true)
            
            // Clear current post fields
            setImageUri(null)
            setTitleText(null)
            
        }
    }
    
    
    private data class UploadMediaResponse(
        val media: WPMedia? = null,
        val errorMessage: String? = null
    )
    
    
    private data class UploadPostResponse(
        val uploadedPost: WPBlogPost? = null,
        val errorMessage: String? = null
    )
    
    
    private suspend fun uploadMedia(
        blogId: Int,
        file: File,
        mimeType: MediaType,
        title: String? = null,
        caption: String? = null,
        description: String? = null,
        alt: String? = null
    ) = suspendCoroutine<UploadMediaResponse> {cont ->
        
        val imageBody = file.asRequestBody(mimeType)
    
        val filePart: MultipartBody.Part = MultipartBody.Part.createFormData(
            "media[0]",
            file.name,
            imageBody
        )
        
        val attrs = listOf(
            WPBlogPost.MediaAttributes(
                title = title ?: file.nameWithoutExtension,
                caption = caption ?: "",
                description = description ?: "",
                alt = alt ?: title ?: file.nameWithoutExtension
            )
        )
    
        ApiClient().getApiService(applicationContext)
            .uploadMedia(
                blogId = blogId.toString(),
                media = filePart,
                attrs = attrs,
                fields = WPMedia.FIELDS_STRING
            ).enqueue(object: Callback<ApiService.UploadMediaResponse>{
                
                override fun onFailure(call: Call<ApiService.UploadMediaResponse>, t: Throwable) {
                    // Error creating post
                    Timber.w(t, "Error uploading media!")
                    cont.resume(UploadMediaResponse(errorMessage = "Error uploading media: ${t.localizedMessage}"))
                }
                
                override fun onResponse(call: Call<ApiService.UploadMediaResponse>, response: Response<ApiService.UploadMediaResponse>) {
                    val uploadMediaResponse = response.body()
                    
                    if (uploadMediaResponse == null) {
                        Timber.w("Uploading media: No response received :(")
                        cont.resume(UploadMediaResponse(errorMessage = "Error uploading media: No response received"))
                        return
                    }
                    
                    
                    if (!uploadMediaResponse.errors.isNullOrEmpty()) {
                        cont.resume(UploadMediaResponse(errorMessage = "Error uploading media: ${uploadMediaResponse.errors[0]}"))
                        return
                    }
                    
                    if (uploadMediaResponse.media.isNullOrEmpty()) {
                        cont.resume(UploadMediaResponse(errorMessage = "Error uploading media: No media details returned."))
                        return
                    }
                    
                    Timber.v("Media uploaded! $uploadMediaResponse")
                    cont.resume(UploadMediaResponse(media = uploadMediaResponse.media[0]))
                    
                }
            })
    
    }
    
    private val bareTemplate = """
            <p class="has-text-color has-small-font-size has-dark-gray-color">
                Published using <a href="https://play.google.com/store/apps/details?id=net.c306.photopress">PhotoPress for Android</a>
            </p>
        """.trimIndent()
    
    private val gallerySingleTemplate = """
            [gallery ids="MEDIA_ID" columns="1" size="large"]
            <p class="has-text-color has-small-font-size has-dark-gray-color">
                Published using <a href="https://play.google.com/store/apps/details?id=net.c306.photopress">PhotoPress for Android</a>
            </p>
        """.trimIndent()
    
    
    private suspend fun uploadPost(
        blogId: Int,
        media: WPMedia,
        blogTitle: String
    ) = suspendCoroutine<UploadPostResponse> {cont ->
        
        val content = gallerySingleTemplate
            .replace("MEDIA_ID", media.id.toString())
        
        
        ApiClient().getApiService(applicationContext)
            .uploadBlogpost(
                blogId = blogId.toString(),
                fields = BlogPostRequest.FIELDS_STRING,
                body = BlogPostRequest(
                    title = blogTitle,
                    content = content,
                    status = WPBlogPost.PublishStatus.DRAFT
                )
            )
            .enqueue(object : Callback<WPBlogPost> {
                override fun onFailure(call: Call<WPBlogPost>, t: Throwable) {
                    // Error creating post
                    Timber.w(t, "Error uploading blogpost!")
                    cont.resume(UploadPostResponse(errorMessage = "Error uploading blogpost: ${t.localizedMessage}"))
                }
                
                override fun onResponse(
                    call: Call<WPBlogPost>,
                    response: Response<WPBlogPost>
                ) {
                    val publishResponse = response.body()
                    
                    if (publishResponse == null) {
                        Timber.w("Upload post: No response received :(")
                        cont.resume(UploadPostResponse(errorMessage = "Error uploading post: No response received"))
                        return
                    }
                    
                    Timber.v("Blog uploaded! $publishResponse")
                    cont.resume(UploadPostResponse(uploadedPost = publishResponse))
                }
            })
    }
    
    
    private suspend fun updateToPublished(
        blogId: Int,
        blogPost: WPBlogPost
    ) = suspendCoroutine<UploadPostResponse> {cont ->
        
        ApiClient().getApiService(applicationContext)
            .updatePostStatus(
                blogId = blogId.toString(),
                postId = blogPost.id.toString(),
                fields = BlogPostRequest.FIELDS_STRING,
                body = ApiService.UpdatePostStatusRequest(
                    status = WPBlogPost.PublishStatus.PUBLISH
                )
            )
            .enqueue(object : Callback<WPBlogPost> {
                override fun onFailure(call: Call<WPBlogPost>, t: Throwable) {
                    // Error creating post
                    Timber.w(t, "Error  updating to published!")
                    cont.resume(UploadPostResponse(errorMessage = "Error  updating to published: ${t.localizedMessage}"))
                }
                
                override fun onResponse(
                    call: Call<WPBlogPost>,
                    response: Response<WPBlogPost>
                ) {
                    val publishResponse = response.body()
                    
                    if (publishResponse == null) {
                        Timber.w("Error updating to published: No blog response received :(")
                        cont.resume(UploadPostResponse(errorMessage = "Error publishing: No response received"))
                        return
                    }
                    
                    Timber.v("Blog published! $publishResponse")
                    cont.resume(UploadPostResponse(uploadedPost = publishResponse))
                }
            })
    }
    
    
}