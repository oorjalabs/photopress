package net.c306.photopress.ui.newPost

import android.app.Application
import android.content.SharedPreferences
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.c306.photopress.R
import net.c306.photopress.UserPrefs
import net.c306.photopress.api.*
import net.c306.photopress.utils.Utils
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okio.Buffer
import okio.IOException
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
        /** Blog not selected. Can't publish. */
        NO_BLOG_SELECTED,
        
        /** Image not selected. Can't publish. */
        EMPTY,
        
        /** Image selected, title text not available. Can't publish. */
        HAVE_IMAGE,
        
        /** Image & title text are both available. Can publish. */
        READY,
        
        /** Publishing post. */
        PUBLISHING,
        
        /** Post. Show link, clear inputs */
        PUBLISHED
    }
    
    private val _state = MutableLiveData<State>().apply { value = State.EMPTY }
    val state: LiveData<State> = _state
    
    internal fun updateState() {
        val title = titleText.value ?: ""
        val image = imageUri.value
        val blogId = selectedBlogId.value
        val publishedPost = publishedPost.value
        
        _state.value = when {
            blogId == null || blogId < 0 -> State.NO_BLOG_SELECTED
            publishedPost != null -> State.PUBLISHED
            image == null -> State.EMPTY
            title.isBlank() -> State.HAVE_IMAGE
            else -> State.READY
        }
    }
    
    val inputsEnabled = Transformations.switchMap(state) {
        MutableLiveData<Boolean>().apply {
            value = it != State.NO_BLOG_SELECTED && it != State.PUBLISHING && it != State.PUBLISHED
        }
    }
    
    
    // Selected Blog
    private val _selectedBlogId = MutableLiveData<Int>()
    private val selectedBlogId: LiveData<Int> = _selectedBlogId
    val selectedBlog = Transformations.switchMap(selectedBlogId) { blogId ->
        val selectedBlog =
            if (blogId == null || blogId < 0) null
            else AuthPrefs(applicationContext)
                .getBlogsList()
                .find { it.id == blogId }
        
        MutableLiveData<Blog?>().apply { value = selectedBlog }
    }
    
    private fun setSelectedBlogId(value: Int) {
        _selectedBlogId.value = value
        
        updateState()
        
        val selectedTags = AuthPrefs(applicationContext).getTagsList()
        setBlogTags(selectedTags ?: emptyList())
        if (selectedTags == null) updateTagsList()
    }
    
    
    // Selected Blog's Tags
    private val _blogTags = MutableLiveData<List<WPTag>>()
    val blogTags: LiveData<List<WPTag>> = _blogTags
    
    private fun setBlogTags(list: List<WPTag>) {
        _blogTags.value = list
    }
    
    private fun updateTagsList() {
        viewModelScope.launch {
            refreshTags().tags?.let {
                setBlogTags(it)
                AuthPrefs(applicationContext).saveTagsList(it)
            }
        }
    }
    
    
    // Image Uri
    private val _imageUri = MutableLiveData<Uri>()
    val imageUri: LiveData<Uri> = _imageUri
    
    fun setImageUri(value: Uri?) {
        _imageUri.value = value
        updateState()
    }
    
    
    // Image File
    val fileDetails = Transformations.switchMap(imageUri) {
        MutableLiveData<FileDetails?>().apply {
            value = if (it == null) null else getFileName(it)
        }
    }
    
    data class FileDetails(
        val fileName: String,
        val mimeType: String
    )
    
    
    // Title text
    val titleText = MutableLiveData<String>()
    
    // Post tags
    val postTags = MutableLiveData<String>()
    
    
    // Published post data
    private val _publishedPost = MutableLiveData<PublishedPost>()
    val publishedPost: LiveData<PublishedPost> = _publishedPost
    
    fun newPost() {
        _publishedPost.value = null
        updateState()
    }
    
    data class PublishedPost(
        val post: WPBlogPost,
        val isDraft: Boolean
    )
    
    
    // Observer for changes to selected blog id
    private val observer = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        when (key) {
            UserPrefs.KEY_SELECTED_BLOG_ID -> setSelectedBlogId(UserPrefs(applicationContext).getSelectedBlogId())
        }
    }
    
    init {
        updateState()
        
        val userPrefs = UserPrefs(applicationContext)
        setSelectedBlogId(userPrefs.getSelectedBlogId())
        userPrefs.observe(observer)
    }
    
    
    /**
     * Takes a content URI and writes the contents to a file in app's storage.
     * Returns the app's copy of file and its mime type
     * @return Pair with file and its mime-type, or null if the uri couldn't be read
     */
    private fun getFileForUri(uri: Uri): Pair<File, String>? {
        
        val fileDetails = getFileName(uri)
        
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
    
    
    /**
     * Gets the file name and mime-type for the given content uri
     */
    private fun getFileName(uri: Uri): FileDetails {
        
        val projection = arrayOf(
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.MIME_TYPE
        )
        
        val metaCursor = applicationContext.contentResolver.query(
            uri,
            projection,
            null,
            null,
            null
        )
        
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
    
    
    fun publishPost() {
        val blogId = selectedBlogId.value
        val title = titleText.value
        val image = imageUri.value
        val tags = postTags.value
            ?.split(",")
            ?.filter { !it.isBlank() }
            ?.distinct()
            ?: emptyList()
        
        
        if (blogId == null || title.isNullOrBlank() || image == null) {
            Timber.w("Null inputs to publish: blogId: '$blogId', title: '$title', image: '$image'")
            Toast.makeText(applicationContext, "Null inputs to publish :(", Toast.LENGTH_LONG)
                .show()
            return
        }
        
        
        viewModelScope.launch {
            
            val imageDetails = getFileForUri(image)
            
            if (imageDetails == null) {
                Timber.w("File not found!: $image")
                return@launch
            }
            
            // Reset published post data
            _publishedPost.value = null
            _state.value = State.PUBLISHING
            
            val (file, mimeType) = imageDetails
            
            // Upload media to WP
            val (media, mediaError) = uploadMedia(
                blogId,
                file,
                mimeType.toMediaType()
            )
            
            if (mediaError != null || media == null) {
                // Show error message and reset state
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        applicationContext,
                        mediaError ?: "No media returned",
                        Toast.LENGTH_LONG
                    ).show()
                    _state.value = State.READY
                }
                return@launch
            }
            
            // Upload post as draft with embedded image
            val (uploadedPost, uploadError) = uploadPost(
                blogId,
                media,
                title,
                tags
            )
            
            if (uploadError != null || uploadedPost == null) {
                // Show error message and reset state
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        applicationContext,
                        uploadError ?: "Uploaded post not returned",
                        Toast.LENGTH_LONG
                    ).show()
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
                Toast.makeText(applicationContext, "Post uploaded as draft.", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(
                    applicationContext,
                    "Post published successfully.",
                    Toast.LENGTH_SHORT
                ).show()
            }
            
            
            val blogTags = (blogTags.value ?: emptyList())
            val newTags = (publishedPost?.tags ?: uploadedPost.tags)
                .values
                .filter { tag -> blogTags.none { it.id == tag.id } }
            
            // If there were new tags found, save them to blog tags list
            if (newTags.isNotEmpty()) {
                val updatedBlogTags = blogTags.toMutableList().apply {
                    addAll(newTags)
                }
                
                AuthPrefs(applicationContext).saveTagsList(updatedBlogTags)
                setBlogTags(updatedBlogTags)
            }
            
            
            Timber.d("Blogpost done! ${publishedPost ?: uploadedPost}")
            
            // Update published post
            _publishedPost.value = publishedPost
                ?.let { PublishedPost(it, false) }
                ?: PublishedPost(uploadedPost, true)
            
            // Clear current post fields
            setImageUri(null)
            titleText.value = null
            postTags.value = null
            
            updateState()
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
    ) = suspendCoroutine<UploadMediaResponse> { cont ->
        
        val imageBody = file.asRequestBody(mimeType)
        
        val filePart: MultipartBody.Part = MultipartBody.Part.createFormData(
            "media[0]",
            file.name,
            imageBody
        )
        
        val attrs = listOf(
            WPMedia.MediaAttributes(
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
            ).enqueue(object : Callback<WPMedia.UploadMediaResponse> {
                
                override fun onFailure(call: Call<WPMedia.UploadMediaResponse>, t: Throwable) {
                    // Error creating post
                    Timber.w(t, "Error uploading media!")
                    cont.resume(UploadMediaResponse(errorMessage = "Error uploading media: ${t.localizedMessage}"))
                }
                
                override fun onResponse(
                    call: Call<WPMedia.UploadMediaResponse>,
                    response: Response<WPMedia.UploadMediaResponse>
                ) {
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
    
    
    @Suppress("unused")
    private val singleImageClassicTemplate = """
            [gallery ids="%%MEDIA_ID%%" columns="1" size="large"]
            <p class="has-text-color has-small-font-size has-dark-gray-color">
                Published using <a href="https://play.google.com/store/apps/details?id=net.c306.photopress">PhotoPress for Android</a>
            </p>
        """.trimIndent()
    
    private val singleImageBlockTemplate = """
        <!-- wp:image {"id":%%MEDIA_ID%%,"align":"center","linkDestination":"media"} -->
        <div class="wp-block-image"><figure class="aligncenter"><a href="%%MEDIA_URL%%"><img src="%%MEDIA_LARGE%%" alt="" class="wp-image-%%MEDIA_ID%%"/></a><figcaption>%%MEDIA_CAPTION%%</figcaption></figure></div>
        <!-- /wp:image -->
        
        <!-- wp:more -->
        <!--more-->
        <!-- /wp:more -->
        
        <!-- wp:paragraph {"fontSize":"small"} -->
        <p class="has-small-font-size">Published using <a href="https://play.google.com/store/apps/details?id=net.c306.photopress">PhotoPress for Android</a></p>
        <!-- /wp:paragraph -->
    """.trimIndent()
    
    private suspend fun uploadPost(
        blogId: Int,
        media: WPMedia,
        title: String,
        tags: List<String>
    ) = suspendCoroutine<UploadPostResponse> { cont ->
        
//        val content = gallerySingleTemplate
//            .replace("%%MEDIA_ID%%", media.id.toString())
        val content = singleImageBlockTemplate
            .replace("%%MEDIA_ID%%", media.id.toString())
            .replace("%%MEDIA_LARGE%%", media.thumbnails?.large ?: media.url)
            .replace("%%MEDIA_URL%%", media.url)
            .replace("%%MEDIA_CAPTION%%", if (media.caption.isNullOrBlank()) title else media.caption)
            
        
        ApiClient().getApiService(applicationContext)
            .uploadBlogpost(
                blogId = blogId.toString(),
                fields = WPBlogPost.FIELDS_STRING,
                body = WPBlogPost.CreatePostRequest(
                    title = title,
                    content = content,
                    tags = tags,
                    status = WPBlogPost.PublishStatus.DRAFT
                )
            ).enqueue(object : Callback<WPBlogPost> {
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
    
    @Suppress("unused")
    private fun bodyToString(request: Request): String? {
        return try {
            val copy: Request = request.newBuilder().build()
            val buffer = Buffer()
            copy.body?.writeTo(buffer)
            buffer.readUtf8()
        } catch (e: IOException) {
            "did not work"
        }
    }
    
    
    private suspend fun updateToPublished(
        blogId: Int,
        blogPost: WPBlogPost
    ) = suspendCoroutine<UploadPostResponse> { cont ->
        
        ApiClient().getApiService(applicationContext)
            .updatePostStatus(
                blogId = blogId.toString(),
                postId = blogPost.id.toString(),
                fields = WPBlogPost.FIELDS_STRING,
                body = WPBlogPost.UpdatePostStatusRequest(
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
    
    
    private data class RefreshTagsResult(
        val errorMessage: String? = null,
        val tags: List<WPTag>? = null
    )
    
    private suspend fun refreshTags() = suspendCoroutine<RefreshTagsResult> { cont ->
        val blogId = selectedBlogId.value?.toString()
        
        if (blogId.isNullOrBlank()) {
            cont.resume(RefreshTagsResult(errorMessage = "No blog selected"))
            return@suspendCoroutine
        }
        
        ApiClient().getApiService(applicationContext)
            .getTagsForSite(blogId)
            .enqueue(object : Callback<WPTag.TagsResponse> {
                override fun onFailure(call: Call<WPTag.TagsResponse>, t: Throwable) {
                    // Error creating post
                    Timber.w(t, "Error fetching tags!")
                    cont.resume(RefreshTagsResult(errorMessage = "Error fetching tags: ${t.localizedMessage}"))
                }
                
                override fun onResponse(
                    call: Call<WPTag.TagsResponse>,
                    response: Response<WPTag.TagsResponse>
                ) {
                    val fetchTagsResponse = response.body()
                    
                    if (fetchTagsResponse == null) {
                        Timber.w("Error updating to published: No blog response received :(")
                        cont.resume(RefreshTagsResult(errorMessage = "Error publishing: No response received"))
                        return
                    }
                    
                    Timber.v("Fetched ${fetchTagsResponse.found} tags")
                    cont.resume(RefreshTagsResult(tags = fetchTagsResponse.tags))
                }
            })
    }
    
    val publishedDialogMessage: String
        get() {
            val published = publishedPost.value ?: return ""
            return applicationContext.getString(
                if (published.isDraft) R.string.message_post_draft
                else R.string.message_post_published,
                published.post.title
            )
        }
    
    fun sharePost(post: WPBlogPost) {
        Utils.sendSharingIntent(applicationContext, post.url, post.title)
    }
    
    fun openPostExternal(post: WPBlogPost) {
        applicationContext.startActivity(Utils.getIntentForUrl(post.url))
    }
    
    fun copyPostToClipboard(post: WPBlogPost) {
        Utils.copyToClipboard(applicationContext, "${post.title}\n${post.url}")
    }
}