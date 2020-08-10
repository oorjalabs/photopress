package net.c306.photopress.sync

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.c306.photopress.R
import net.c306.photopress.api.ApiClient
import net.c306.photopress.api.WPBlogPost
import net.c306.photopress.api.WPCategory
import net.c306.photopress.api.WPMedia
import net.c306.photopress.database.PhotoPressPost
import net.c306.photopress.database.PostImage
import net.c306.photopress.utils.UserPrefs
import net.c306.photopress.utils.Utils
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import java.io.File
import java.time.Instant
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class SyncUtils(context: Context) {
    
    private val applicationContext = context.applicationContext
    
    data class PublishLiveData(
        val progress: PublishProgress,
        val response: PublishPostResponse? = null
    )
    
    data class PublishProgress(
        val finished: Boolean,
        val statusMessage: String
    )
    
    data class PublishPostResponse(
        val errorMessage: String?,
        /** Not null if publishing successful */
        val publishedPost: PublishedPost?,
        /** Present if image uploading produced an error. If some images were uploaded, they'll be present here */
        val updatedImages: List<UploadMediaResponse>?
    )
    
    data class UploadMediaResponse(
        val errorMessage: String?,
        val media: WPMedia?,
        val originalImage: PostImage
    )
    
    data class PublishedPost(
        val post: WPBlogPost,
        val isDraft: Boolean
    )
    
    private data class UploadPostResponse(
        val uploadedPost: WPBlogPost? = null,
        val errorMessage: String? = null
    )
    
    
    fun publishPostLiveData(
        post: PhotoPressPost,
        images: List<PostImage>,
        addFeaturedImage: Boolean?,
        useBlockEditor: Boolean?,
        isJetpackBlog: Boolean?
    ): LiveData<PublishLiveData?> = liveData<PublishLiveData?> {
        
        emit(PublishLiveData(progress = PublishProgress(
            finished = false,
            statusMessage = applicationContext.getString(R.string.sync_status_started_upload)
        )))
        
        withContext(Dispatchers.IO) {
            
            if (post.title.isBlank() || images.isEmpty()) {
                Timber.w("Null inputs to publish: title: '${post.title}', ${images.size} images")
                val errorMessage = applicationContext.getString(R.string.sync_status_error_no_title_or_images)
                val response = PublishPostResponse(
                    errorMessage = errorMessage,
                    publishedPost = null,
                    updatedImages = null
                )
                val progress = PublishProgress(
                    finished = true,
                    statusMessage = errorMessage
                )
                emit(PublishLiveData(progress, response))
                return@withContext
            }
            
            emit(PublishLiveData(progress = PublishProgress(
                finished = false,
                statusMessage = applicationContext.getString(R.string.sync_status_uploading_images)
            )))
            
            val mediaToUpload = images.mapNotNull { image ->
                getFileForUri(applicationContext, image)?.let { (file, _) ->
                    Pair(file, image)
                }
            }
            
            var uploadSuccess = true
            val uploadedMedia: List<UploadMediaResponse>
            
            if (isJetpackBlog == true) {
                // Upload images one by one
                val imageCount = mediaToUpload.size
                uploadedMedia = mediaToUpload.mapIndexed { index, imagePair ->
                    emit(PublishLiveData(progress = PublishProgress(
                        finished = false,
                        statusMessage = applicationContext.getString(R.string.sync_status_uploading_images_individual, index + 1, imageCount)
                    )))
                    val (success, uploaded) = uploadSingleMedia(post.blogId, imagePair)
                    uploadSuccess = success && uploadSuccess
                    if (!success) Timber.d("Error uploading image $index: '${uploaded.errorMessage}'")
                    uploaded
                }
            } else {
                // Upload images all together
                val uploaded = uploadMedia(post.blogId, mediaToUpload)
                uploadSuccess = uploaded.first
                uploadedMedia = uploaded.second
            }
            
            // Delete images from app storage
            deleteFiles(mediaToUpload.map { it.first })
            
            if (!uploadSuccess) {
                val errorMessage = applicationContext.getString(
                    R.string.sync_status_error,
                    uploadedMedia[0].errorMessage
                    ?: applicationContext.getString(R.string.sync_status_error_media_upload_failed)
                )
                val response = PublishPostResponse(
                    errorMessage = errorMessage,
                    publishedPost = null,
                    updatedImages = null
                )
                val progress = PublishProgress(true, errorMessage)
                emit(PublishLiveData(progress, response))
                
                return@withContext
            }
            
            if (isJetpackBlog == true && false) {
                // Disabled because WP api returns 404 for recently updated image :(
                Timber.v("Jetpack blog, updating media attributes again")
                uploadedMedia.forEach { updateMediaMetadata(post.blogId, it) }
            }
            
            emit(PublishLiveData(progress = PublishProgress(
                finished = false,
                statusMessage = applicationContext.getString(R.string.sync_status_uploading_post)
            )))
            
            // Upload post as draft with embedded image
            val (uploadedPost, uploadError) = uploadPost(
                post = post,
                postImages = uploadedMedia,
                useBlockEditor = useBlockEditor,
                addFeaturedImage = addFeaturedImage
            )
            
            if (uploadError != null || uploadedPost == null) {
                val errorMessage = applicationContext.getString(
                    R.string.sync_status_error,
                    uploadError
                    ?: applicationContext.getString(R.string.sync_status_error_post_upload_failed)
                )
                val response = PublishPostResponse(
                    errorMessage = errorMessage,
                    publishedPost = null,
                    updatedImages = uploadedMedia
                )
                val progress = PublishProgress(true, errorMessage)
                emit(PublishLiveData(progress, response))
                
                return@withContext
            }
            
            var publishedPost: WPBlogPost? = null
            
            if (post.status != PhotoPressPost.PhotoPostStatus.DRAFT) {
                
                emit(PublishLiveData(progress = PublishProgress(
                    finished = false,
                    statusMessage = applicationContext.getString(R.string.sync_status_updating_post_status)
                )))
                
                // Change status to published
                val publishResult = updateToPublished(
                    post.blogId,
                    uploadedPost,
                    post.scheduledTime
                )
                publishedPost = publishResult.uploadedPost
            }
            
            Timber.d("Blogpost done! ${publishedPost ?: uploadedPost}")
            
            // Update published post
            val response = PublishPostResponse(
                errorMessage = null,
                publishedPost = PublishedPost(publishedPost ?: uploadedPost, publishedPost == null),
                updatedImages = null
            )
            val progress = PublishProgress(
                finished = true,
                statusMessage = applicationContext.getString(R.string.sync_status_post_uploaded)
            )
            emit(PublishLiveData(progress, response))
        }
    }
    
    
    private suspend fun uploadMedia(
        blogId: Int,
        images: List<Pair<File, PostImage>>
    ) = suspendCoroutine<Pair<Boolean, List<UploadMediaResponse>>> { cont ->
        
        val requestBodyBuilder = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
        
        images.forEachIndexed { index, (file, image) ->
            
            val imageBody = file.asRequestBody(image.fileDetails!!.mimeType.toMediaType())
            
            requestBodyBuilder.addFormDataPart(
                "media[${index}]",
                image.name ?: file.name,
                imageBody
            )
            
            requestBodyBuilder.addFormDataPart(
                "attrs[${index}][caption]",
                image.caption ?: ""
            )
            requestBodyBuilder.addFormDataPart(
                "attrs[${index}][title]",
                image.name ?: file.nameWithoutExtension
            )
            requestBodyBuilder.addFormDataPart(
                "attrs[${index}][alt]",
                image.altText ?: image.name ?: file.nameWithoutExtension
            )
            requestBodyBuilder.addFormDataPart(
                "attrs[${index}][description]",
                image.description ?: ""
            )
            
        }
        
        
        ApiClient().getApiService(applicationContext)
            .uploadMediaMulti(
                blogId = blogId.toString(),
                contents = requestBodyBuilder.build(),
                fields = WPMedia.FIELDS_STRING
            ).enqueue(object : Callback<WPMedia.UploadMediaResponse> {
                
                override fun onFailure(call: Call<WPMedia.UploadMediaResponse>, t: Throwable) {
                    // Error creating post
                    Timber.w(t, "Error uploading media: ")
                    cont.resume(
                        Pair(
                            false,
                            images.map { UploadMediaResponse(t.message, null, it.second) })
                    )
                }
                
                override fun onResponse(
                    call: Call<WPMedia.UploadMediaResponse>,
                    response: Response<WPMedia.UploadMediaResponse>
                ) {
                    val uploadMediaResponse = response.body()
                    
                    if (uploadMediaResponse == null) {
                        Timber.w("Error uploading media: No response received :(")
                        cont.resume(
                            Pair(
                                false,
                                images.map {
                                    UploadMediaResponse(
                                        applicationContext.getString(R.string.sync_status_error_no_response),
                                        null,
                                        it.second
                                    )
                                })
                        )
                        return
                    }
                    
                    
                    if (!uploadMediaResponse.errors.isNullOrEmpty()) {
                        Timber.w("Error uploading media: ${uploadMediaResponse.errors.joinToString("\n")}")
                        cont.resume(
                            Pair(
                                false,
                                images.map {
                                    UploadMediaResponse(
                                        uploadMediaResponse.errors[0],
                                        null,
                                        it.second
                                    )
                                })
                        )
                        return
                    }
                    
                    if (uploadMediaResponse.media.isNullOrEmpty()) {
                        Timber.w("Error uploading media: No media returned")
                        cont.resume(
                            Pair(
                                false,
                                images.map {
                                    UploadMediaResponse(
                                        applicationContext.getString(R.string.sync_status_error_no_media_returned),
                                        null,
                                        it.second
                                    )
                                })
                        )
                        return
                    }
                    
                    Timber.v("Media uploaded! $uploadMediaResponse")
                    cont.resume(Pair(true, images.mapIndexed { index, (_, image) ->
                        UploadMediaResponse(
                            errorMessage = null,
                            media = uploadMediaResponse.media.getOrNull(index),
                            originalImage = image
                        )
                    }))
                    
                }
            })
        
    }
    
    
    private suspend fun uploadSingleMedia(
        blogId: Int,
        imagePair: Pair<File, PostImage>
    ) = suspendCoroutine<Pair<Boolean, UploadMediaResponse>> { cont ->
        
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .apply {
                
                val (file, image) = imagePair
                
                val imageBody = file.asRequestBody(image.fileDetails!!.mimeType.toMediaType())
                
                addFormDataPart(
                    "media[0]",
                    image.name ?: file.name,
                    imageBody
                )
                addFormDataPart(
                    "attrs[0][caption]",
                    image.caption ?: ""
                )
                addFormDataPart(
                    "attrs[0][title]",
                    image.name ?: file.nameWithoutExtension
                )
                addFormDataPart(
                    "attrs[0][alt]",
                    image.altText ?: image.name ?: file.nameWithoutExtension
                )
                addFormDataPart(
                    "attrs[0][description]",
                    image.description ?: ""
                )
                
            }
            .build()
        
        
        ApiClient().getApiService(applicationContext)
            .uploadSingleMedia(
                blogId = blogId.toString(),
                contents = requestBody,
                fields = WPMedia.FIELDS_STRING
            ).enqueue(object : Callback<WPMedia.UploadMediaResponse> {
                
                override fun onFailure(call: Call<WPMedia.UploadMediaResponse>, t: Throwable) {
                    // Error creating post
                    Timber.w(t, "Error uploading media: ")
                    cont.resume(
                        Pair(
                            false,
                            UploadMediaResponse(t.message, null, imagePair.second)
                        )
                    )
                }
                
                override fun onResponse(
                    call: Call<WPMedia.UploadMediaResponse>,
                    response: Response<WPMedia.UploadMediaResponse>
                ) {
                    val uploadMediaResponse = response.body()
                    
                    if (uploadMediaResponse == null) {
                        Timber.w("Error uploading media: No response received :(")
                        cont.resume(
                            Pair(
                                false,
                                UploadMediaResponse(
                                    errorMessage = applicationContext.getString(R.string.sync_status_error_no_response),
                                    media = null,
                                    originalImage = imagePair.second
                                )
                            )
                        )
                        return
                    }
                    
                    
                    if (!uploadMediaResponse.errors.isNullOrEmpty()) {
                        Timber.w("Error uploading media: ${uploadMediaResponse.errors.joinToString("\n")}")
                        cont.resume(
                            Pair(
                                false,
                                UploadMediaResponse(uploadMediaResponse.errors[0], null, imagePair.second)
                            )
                        )
                        return
                    }
                    
                    if (uploadMediaResponse.media.isNullOrEmpty()) {
                        Timber.w("Error uploading media: No media returned")
                        cont.resume(
                            Pair(
                                false,
                                UploadMediaResponse(
                                    errorMessage = applicationContext.getString(R.string.sync_status_error_no_media_returned),
                                    media = null,
                                    originalImage = imagePair.second
                                )
                            )
                        )
                        return
                    }
                    
                    Timber.v("Media uploaded! $uploadMediaResponse")
                    cont.resume(
                        Pair(
                            true,
                            UploadMediaResponse(
                                errorMessage = null,
                                media = uploadMediaResponse.media.getOrNull(0),
                                originalImage = imagePair.second
                            )
                        )
                    )
                    
                }
            })
        
    }
    
    
    private suspend fun updateMediaMetadata(
        blogId: Int,
        image: UploadMediaResponse
    ) = suspendCoroutine<UploadMediaResponse> { cont ->
        
        ApiClient().getApiService(applicationContext)
            .updateMediaAttributes(
                blogId = blogId.toString(),
                mediaId = image.media!!.id.toString(),
                fields = WPMedia.FIELDS_STRING,
                body = WPMedia.UpdateMediaAttributesRequest(
                    title = image.originalImage.name,
                    caption = image.originalImage.caption,
                    description = image.originalImage.description,
                    alt = image.originalImage.altText
                )
            )
            .enqueue(object : Callback<WPMedia> {
                
                override fun onFailure(call: Call<WPMedia>, t: Throwable) {
                    // Error creating post
                    Timber.w(t, "Error uploading media!")
                    cont.resume(image.copy(errorMessage = t.message))
                }
                
                override fun onResponse(
                    call: Call<WPMedia>,
                    response: Response<WPMedia>
                ) {
                    val updateMediaResponse = response.body()
                    
                    if (updateMediaResponse == null) {
                        Timber.w("Updating media: No response received :(")
                        cont.resume(image.copy(errorMessage = "No response"))
                        return
                    }
                    
                    Timber.v("Media updated! $updateMediaResponse")
                    cont.resume(image.copy(media = updateMediaResponse))
                }
            })
    }
    
    
    private suspend fun uploadPost(
        post: PhotoPressPost,
        postImages: List<UploadMediaResponse>,
        useBlockEditor: Boolean?,
        addFeaturedImage: Boolean?
    ) = suspendCoroutine<UploadPostResponse> { cont ->
        
        val usingBlockEditor = useBlockEditor ?: UserPrefs.DEFAULT_USE_BLOCK_EDITOR
        val addingFeaturedImage = addFeaturedImage ?: UserPrefs.DEFAULT_ADD_FEATURED_IMAGE
        
        val images = postImages
            .filter { it.media != null }
            .sortedBy { it.originalImage.order }
        
        val galleryImagesString = images.joinToString(",") { it.media!!.id.toString() }
        
        val columnCount = Utils.calculateColumnCount(images.size)
        
        val content = when {
            
            usingBlockEditor && images.size == 1 -> {
                (BLOCK_TEMPLATE_SINGLE_IMAGE + BLOCK_TEMPLATE_FOOTER)
                    .replace("%%MEDIA_ID%%", images[0].media!!.id.toString())
                    .replace("%%MEDIA_ALT%%", images[0].media!!.alt ?: "")
                    .replace(
                        "%%MEDIA_LARGE%%",
                        images[0].media!!.thumbnails?.large ?: images[0].media!!.url
                    )
                    .replace("%%MEDIA_URL%%", images[0].media!!.url)
                    .replace("%%MEDIA_CAPTION%%", images[0].media!!.caption ?: post.title)
                
            }
            
            images.size == 1                     -> {
                CLASSIC_TEMPLATE_SINGLE_IMAGE
                    .replace("%%MEDIA_ID%%", images[0].media!!.id.toString())
            }
            
            usingBlockEditor                     -> {
                // Top section of gallery post including image ids
                BLOCK_TEMPLATE_GALLERY_TOP
                    .replace("%%MEDIA_ID_LIST%%", galleryImagesString)
                    .replace("%%COLUMN_COUNT%%", columnCount.toString()) +
                // Images
                images.joinToString("\n") {
                    val imageMedia = it.media!!
                    BLOCK_TEMPLATE_GALLERY_IMAGE
                        .replace("%%MEDIA_ID%%", imageMedia.id.toString())
                        .replace("%%MEDIA_ALT%%", imageMedia.alt ?: "")
                        .replace(
                            "%%MEDIA_LARGE%%",
                            imageMedia.thumbnails?.large ?: imageMedia.url
                        )
                        .replace("%%MEDIA_URL%%", imageMedia.url)
                        .replace("%%MEDIA_CAPTION%%", imageMedia.caption ?: post.title)
                } +
                // Bottom section of gallery post, including post caption
                BLOCK_TEMPLATE_GALLERY_BOTTOM.replace("%%POST_CAPTION%%", post.postCaption) +
                // Photopress footer
                BLOCK_TEMPLATE_FOOTER
            }
            
            else                                 -> {
                CLASSIC_TEMPLATE_GALLERY
                    .replace("%%MEDIA_ID_LIST%%", galleryImagesString)
                    .replace("%%POST_CAPTION%%", post.postCaption)
                    .replace("%%COLUMN_COUNT%%", columnCount.toString())
            }
        }
        
        // Set featuredImage to specified postThumbnail or to first image
        val featuredImage = images.find { it.originalImage.id == post.postThumbnail }
                            ?: images[0]
        
        ApiClient().getApiService(applicationContext)
            .uploadBlogpost(
                blogId = post.blogId.toString(),
                fields = WPBlogPost.FIELDS_STRING,
                body = WPBlogPost.CreatePostRequest(
                    title = post.title,
                    content = content.trimIndent(),
                    tags = post.tags,
                    categories = post.categories,
                    status = WPBlogPost.PublishStatus.DRAFT,
                    featuredImage = if (addingFeaturedImage) featuredImage.media!!.id.toString() else null
                )
            ).enqueue(object : Callback<WPBlogPost> {
                override fun onFailure(call: Call<WPBlogPost>, t: Throwable) {
                    // Error creating post
                    Timber.w(t, "Error uploading blogpost!")
                    cont.resume(UploadPostResponse(errorMessage = t.localizedMessage))
                }
                
                override fun onResponse(
                    call: Call<WPBlogPost>,
                    response: Response<WPBlogPost>
                ) {
                    val publishResponse = response.body()
                    
                    if (publishResponse == null) {
                        Timber.w("Upload post: No response received :(")
                        cont.resume(UploadPostResponse(
                            errorMessage = applicationContext.getString(R.string.sync_status_error_no_response)
                        ))
                        return
                    }
                    
                    Timber.v("Blog uploaded! $publishResponse")
                    cont.resume(UploadPostResponse(uploadedPost = publishResponse))
                }
            })
    }
    
    
    private suspend fun updateToPublished(
        blogId: Int,
        blogPost: WPBlogPost,
        scheduledTime: Long?
    ) = suspendCoroutine<UploadPostResponse> { cont ->
        
        val scheduledDateString = scheduledTime?.let { Instant.ofEpochMilli(it).toString() }
        
        ApiClient().getApiService(applicationContext)
            .updatePostStatus(
                blogId = blogId.toString(),
                postId = blogPost.id.toString(),
                fields = WPBlogPost.FIELDS_STRING,
                body = WPBlogPost.UpdatePostStatusRequest(
                    status = WPBlogPost.PublishStatus.PUBLISH,
                    date = scheduledDateString
                )
            )
            .enqueue(object : Callback<WPBlogPost> {
                override fun onFailure(call: Call<WPBlogPost>, t: Throwable) {
                    // Error creating post
                    Timber.w(t, "Error  updating to published!")
                    cont.resume(UploadPostResponse(errorMessage = t.localizedMessage))
                }
                
                override fun onResponse(
                    call: Call<WPBlogPost>,
                    response: Response<WPBlogPost>
                ) {
                    val publishResponse = response.body()
                    
                    if (publishResponse == null) {
                        Timber.w("Error updating to published: No blog response received :(")
                        cont.resume(UploadPostResponse(
                            errorMessage = applicationContext.getString(R.string.sync_status_error_no_response)
                        ))
                        return
                    }
                    
                    Timber.v("Blog published! $publishResponse")
                    cont.resume(UploadPostResponse(uploadedPost = publishResponse))
                }
            })
    }
    
    
    /**
     * Takes a content URI and writes the contents to a file in app's storage.
     * Returns the app's copy of file and its mime type
     * @return Pair with file and its mime-type, or null if the uri couldn't be read
     */
    private suspend fun getFileForUri(context: Context, postImage: PostImage): Pair<File, String>? =
        withContext(Dispatchers.IO) {
            
            if (postImage.fileDetails == null) return@withContext null
            
            // Open selected file as input stream
            return@withContext context.applicationContext.contentResolver
                .openInputStream(postImage.uri)?.let { stream ->
                    
                    // Write it to app's storage as file
                    val imageFile = File(
                        context.applicationContext.filesDir,
                        postImage.fileDetails.fileName
                    )
                    
                    imageFile.outputStream().use {
                        stream.copyTo(it)
                    }
                    
                    Pair(imageFile, postImage.fileDetails.mimeType)
                }
        }
    
    
    /**
     * Deletes all provided images from app's storage space
     */
    private suspend fun deleteFiles(images: List<File>) = withContext(Dispatchers.IO) {
        
        if (images.isEmpty()) return@withContext
        
        try {
            images
                .filter { it.exists() && it.isFile }
                .forEach { it.delete() }
        } catch (e: SecurityException) {
        }
    }
    
    
    suspend fun addCategory(blogId: Int, categoryName: String): Boolean = suspendCoroutine { cont ->
        
        ApiClient().getApiService(applicationContext)
            .addCategory(
                blogId = blogId.toString(),
                request = WPCategory.AddCategoryRequest(categoryName).toFieldMap()
            )
            .enqueue(object : Callback<WPCategory> {
                
                override fun onFailure(call: Call<WPCategory>, t: Throwable) {
                    // Error creating post
                    Timber.w(t, "Error adding category!")
                    cont.resume(false)
                }
                
                override fun onResponse(
                    call: Call<WPCategory>,
                    response: Response<WPCategory>
                ) {
                    val addCategoryResponse = response.body()
                    
                    if (addCategoryResponse == null) {
                        Timber.w("Adding category: No response received :(")
                        cont.resume(false)
                        return
                    }
                    
                    Timber.v("Category added! $addCategoryResponse")
                    cont.resume(true)
                }
            })
        
    }
    
    
    companion object {
        
        private const val CLASSIC_TEMPLATE_SINGLE_IMAGE = """
            [gallery ids="%%MEDIA_ID%%" columns="1" size="large"]
            <p class="has-text-color has-small-font-size has-dark-gray-color">
                Published using <a href="https://play.google.com/store/apps/details?id=net.c306.photopress">PhotoPress for Android</a>
            </p>
        """
        
        private const val BLOCK_TEMPLATE_SINGLE_IMAGE = """
        <!-- wp:image {"id":%%MEDIA_ID%%,"align":"center","linkDestination":"media"} -->
        <div class="wp-block-image"><figure class="aligncenter"><a href="%%MEDIA_URL%%"><img src="%%MEDIA_LARGE%%" alt="%%MEDIA_ALT%%" class="wp-image-%%MEDIA_ID%%"/></a>%%IMAGE_CAPTION%%</figure></div>
        <!-- /wp:image -->
    """
        private const val BLOCK_TEMPLATE_SINGLE_IMAGE_CAPTION = "<figcaption>%%MEDIA_CAPTION%%</figcaption>"
    
        private const val CLASSIC_TEMPLATE_GALLERY = """
            [gallery ids="%%MEDIA_ID_LIST%%" columns="%%COLUMN_COUNT%%" size="large"]
            %%POST_CAPTION%%
            <p class="has-text-color has-small-font-size has-dark-gray-color">
                Published using <a href="https://play.google.com/store/apps/details?id=net.c306.photopress">PhotoPress for Android</a>
            </p>
        """
        
        private const val BLOCK_TEMPLATE_GALLERY_TOP = """
        <!-- wp:gallery {"ids":[%%MEDIA_ID_LIST%%]} -->
            <figure class="wp-block-gallery columns-%%COLUMN_COUNT%% is-cropped">
            <ul class="blocks-gallery-grid">
    """
        private const val BLOCK_TEMPLATE_GALLERY_BOTTOM = """
            </ul>
            %%GALLERY_CAPTION%%
        </figure>
<!-- /wp:gallery -->
    """
        private const val BLOCK_TEMPLATE_GALLERY_IMAGE =
            """<li class="blocks-gallery-item"><figure><img src="%%MEDIA_URL%%" alt="%%MEDIA_ALT%%" data-id="%%MEDIA_ID%%" class="wp-image-%%MEDIA_ID%%" />%%IMAGE_CAPTION%%</figure></li>"""
        
        private const val BLOCK_TEMPLATE_GALLERY_IMAGE_CAPTION = "<figcaption class=\"blocks-gallery-item__caption\">%%MEDIA_CAPTION%%</figcaption>"
        private const val BLOCK_TEMPLATE_GALLERY_CAPTION = "<figcaption class=\"blocks-gallery-caption\">%%POST_CAPTION%%</figcaption>"
        
        private const val BLOCK_TEMPLATE_FOOTER = """
        <!-- wp:more -->
        <!--more-->
        <!-- /wp:more -->
        
        <!-- wp:paragraph {"fontSize":"small"} -->
        <p class="has-small-font-size">Published using <a href="https://play.google.com/store/apps/details?id=net.c306.photopress">PhotoPress for Android</a></p>
        <!-- /wp:paragraph -->
        """
        
    }
}