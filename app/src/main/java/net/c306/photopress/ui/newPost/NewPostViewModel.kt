package net.c306.photopress.ui.newPost

import android.app.Application
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import net.c306.photopress.api.ApiClient
import net.c306.photopress.api.BlogPostRequest
import net.c306.photopress.api.BlogPostResponse
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import java.io.File


class NewPostViewModel(application: Application) : AndroidViewModel(application) {

    private val applicationContext = application.applicationContext


    // Fragment state
    enum class State {
        EMPTY,
        HAVE_IMAGE,
        READY,
        PUBLISHING
    }

    private val _state = MutableLiveData<State>().apply { value = State.EMPTY }
    val state: LiveData<State> = _state

    private fun updateState() {
        _state.value = when {
            imageUri.value == null && titleText.value.isNullOrBlank() -> State.EMPTY
            imageUri.value != null && titleText.value.isNullOrBlank() -> State.HAVE_IMAGE
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



    // Title text
    private val _titleText = MutableLiveData<String>()
    val titleText: LiveData<String> = _titleText

    fun setTitleText(value: String?) {
        _titleText.value = value
        updateState()
    }


    // Published post data
    private val _publishedPost = MutableLiveData<BlogPostResponse>()
    val publishedPost = _publishedPost



    init {
        updateState()
    }



    private fun getFileForUri(uri: Uri): Pair<File, String>? {

        val (name, mimeType) = getFileName(uri)

        // Open a specific media item using ParcelFileDescriptor.
        val resolver = applicationContext.contentResolver

        // Open selected file as input stream
        resolver.openInputStream(uri)?.use { stream ->
            // Write it to app's storage as file
            val imageFile = File(applicationContext.filesDir, name)
            imageFile.outputStream().use {
                stream.copyTo(it)
            }
            return Pair(imageFile, mimeType)
        }

        return null
    }

    private fun getFileName(uri: Uri): Pair<String, String> {
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

        return Pair(fileName, mimeType)
    }

    internal fun publishPost(
        blogId: Int,
        blogTitle: String,
        imageUri: Uri,
        blogContent: String = "<small>Published using PhotoPress for Android</small>"
    ) {

        val imageDetails = getFileForUri(imageUri)

        if (imageDetails == null) {
            Timber.w("File not found!: $imageUri")
            return
        }

        // Reset published post data
        _publishedPost.value = null
        _state.value = State.PUBLISHING

        val file = imageDetails.first
        val mimeType = imageDetails.second.toMediaType()

        val imageBody = file.asRequestBody(mimeType)

        val filePart: MultipartBody.Part = MultipartBody.Part.createFormData(
            "media[0]",
            file.name,
            imageBody
        )

        val title: MultipartBody.Part = MultipartBody.Part.createFormData(
            "title",
            blogTitle
        )

        val content: MultipartBody.Part = MultipartBody.Part.createFormData(
            "content",
            blogContent
        )

        ApiClient().getApiService(applicationContext)
            .createBlogpost(
                blogId = blogId.toString(),
                fields = BlogPostRequest.FIELDS_STRING,
                title = title,
                content = content,
                media = filePart
            )
            .enqueue(object : Callback<BlogPostResponse> {
                override fun onFailure(call: Call<BlogPostResponse>, t: Throwable) {
                    // Error logging in
                    Timber.w(t, "Error publishing blog!")
                    _state.value = State.READY
                }

                override fun onResponse(
                    call: Call<BlogPostResponse>,
                    response: Response<BlogPostResponse>
                ) {
                    val publishResponse = response.body()

                    if (publishResponse == null) {
                        Timber.w("No blog response received :(")
                        _state.value = State.READY
                        return
                    }

                    Timber.i("Blog published! $publishResponse")

                    // Update published post
                    _publishedPost.value = publishResponse

                    // Clear current post fields
                    setImageUri(null)
                    setTitleText(null)
                }
            })
    }

}