package net.c306.photopress.sync

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.c306.photopress.api.ApiClient
import net.c306.photopress.api.WPBlogPost
import net.c306.photopress.api.WPMedia
import net.c306.photopress.database.PhotoPressPost
import net.c306.photopress.database.PostImage
import net.c306.photopress.utils.UserPrefs
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
    
    /**
     * Takes a content URI and writes the contents to a file in app's storage.
     * Returns the app's copy of file and its mime type
     * @return Pair with file and its mime-type, or null if the uri couldn't be read
     */
    private fun getFileForUri(context: Context, postImage: PostImage): Pair<File, String>? {
        
        if (postImage.fileDetails == null) return null
        
        // Open a specific media item using ParcelFileDescriptor.
        val resolver = context.applicationContext.contentResolver
        
        // Open selected file as input stream
        resolver.openInputStream(postImage.uri)?.use { stream ->
            // Write it to app's storage as file
            val imageFile = File(
                context.applicationContext.filesDir,
                postImage.fileDetails.fileName
            )
            imageFile.outputStream().use {
                stream.copyTo(it)
            }
            return Pair(imageFile, postImage.fileDetails.mimeType)
        }
        
        return null
    }
    
    
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
    
    suspend fun publishPost(
        post: PhotoPressPost,
        images: List<PostImage>,
        addFeaturedImage: Boolean?,
        useBlockEditor: Boolean?
    ): PublishPostResponse =
        withContext(Dispatchers.IO) {
            // TODO: 04/08/2020 Return live data with progress status and publishpost response. Update live data as things move along
            if (post.title.isBlank() || images.isEmpty()) {
                Timber.w("Null inputs to publish: title: '${post.title}', ${images.size} images")
                return@withContext PublishPostResponse(
                    errorMessage = "Error: No title or images",
                    publishedPost = null,
                    updatedImages = null
                )
            }
            
            val uploadedMedia = images.map { image ->
                
                if (image.uploadedMediaId != null) return@map UploadMediaResponse(
                    errorMessage = null,
                    media = null,
                    originalImage = image
                )
                
                val imageDetails = getFileForUri(applicationContext, image)
                
                if (imageDetails == null) {
                    Timber.w("File not found!: $image")
                    return@map UploadMediaResponse(
                        errorMessage = "Error: Image file not found!: ${image.name}",
                        media = null,
                        originalImage = image
                    )
                }
                
                // Reset published post data
                val (file, _) = imageDetails
                
                // Upload media to WP
                val (mediaError, media) = uploadMedia(
                    post.blogId,
                    file,
                    image
                )
                
                if (mediaError != null || media == null) {
                    // Show error message and reset state
                    Timber.w("No or null media response!: $image")
                    return@map UploadMediaResponse(
                        errorMessage = "Error: ${mediaError ?: "Null media upload response!"}",
                        media = null,
                        originalImage = image
                    )
                }
                
                UploadMediaResponse(
                    errorMessage = null,
                    media = media,
                    originalImage = image
                )
                
            }
            
            val firstFailedUpload = uploadedMedia.firstOrNull { it.errorMessage != null }
            if (firstFailedUpload != null) {
                return@withContext PublishPostResponse(
                    errorMessage = firstFailedUpload.errorMessage,
                    publishedPost = null,
                    updatedImages = uploadedMedia
                )
            }
            
            // Upload post as draft with embedded image
            val (uploadedPost, uploadError) = uploadPost(
                post = post,
                postImages = uploadedMedia,
                useBlockEditor = useBlockEditor,
                addFeaturedImage = addFeaturedImage
            )
            
            if (uploadError != null || uploadedPost == null) {
                return@withContext PublishPostResponse(
                    errorMessage = "Error: ${uploadError ?: "No response while uploading post."}",
                    publishedPost = null,
                    updatedImages = uploadedMedia
                )
            }
            
            var publishedPost: WPBlogPost? = null
            
            if (post.status != PhotoPressPost.PhotoPostStatus.DRAFT) {
                
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
            return@withContext PublishPostResponse(
                errorMessage = null,
                publishedPost = PublishedPost(publishedPost ?: uploadedPost, publishedPost == null),
                updatedImages = null
            )
        }
    
    
    private suspend fun uploadMedia(
        blogId: Int,
        file: File,
        image: PostImage
    ) = suspendCoroutine<UploadMediaResponse> { cont ->
        
        val imageBody = file.asRequestBody(image.fileDetails!!.mimeType.toMediaType())
        
        val filePart: MultipartBody.Part = MultipartBody.Part.createFormData(
            "media[0]",
            image.name ?: file.name,
            imageBody
        )
        
        val captionAttr = MultipartBody.Part.createFormData(
            "attrs[0][caption]",
            image.caption ?: ""
        )
        val titleAttr = MultipartBody.Part.createFormData(
            "attrs[0][title]",
            image.name ?: file.nameWithoutExtension
        )
        val altAttr = MultipartBody.Part.createFormData(
            "attrs[0][alt]",
            image.altText ?: image.name ?: file.nameWithoutExtension
        )
        val descriptionAttr = MultipartBody.Part.createFormData(
            "attrs[0][description]",
            image.description ?: ""
        )
        
        ApiClient().getApiService(applicationContext)
            .uploadMedia(
                blogId = blogId.toString(),
                media = filePart,
                title = titleAttr,
                caption = captionAttr,
                alt = altAttr,
                description = descriptionAttr,
                fields = WPMedia.FIELDS_STRING
            ).enqueue(object : Callback<WPMedia.UploadMediaResponse> {
                
                override fun onFailure(call: Call<WPMedia.UploadMediaResponse>, t: Throwable) {
                    // Error creating post
                    Timber.w(t, "Error uploading media!")
                    cont.resume(
                        UploadMediaResponse(
                            errorMessage = "Error uploading media: ${t.localizedMessage}",
                            originalImage = image,
                            media = null
                        )
                    )
                }
                
                override fun onResponse(
                    call: Call<WPMedia.UploadMediaResponse>,
                    response: Response<WPMedia.UploadMediaResponse>
                ) {
                    val uploadMediaResponse = response.body()
                    
                    if (uploadMediaResponse == null) {
                        Timber.w("Uploading media: No response received :(")
                        cont.resume(
                            UploadMediaResponse(
                                errorMessage = "Error uploading media: No response received",
                                media = null,
                                originalImage = image
                            )
                        )
                        return
                    }
                    
                    
                    if (!uploadMediaResponse.errors.isNullOrEmpty()) {
                        cont.resume(
                            UploadMediaResponse(
                                errorMessage = "Error uploading media: ${uploadMediaResponse.errors[0]}",
                                media = null,
                                originalImage = image
                            )
                        )
                        return
                    }
                    
                    if (uploadMediaResponse.media.isNullOrEmpty()) {
                        cont.resume(
                            UploadMediaResponse(
                                errorMessage = "Error uploading media: No media details returned.",
                                media = null,
                                originalImage = image
                            )
                        )
                        return
                    }
                    
                    Timber.v("Media uploaded! $uploadMediaResponse")
                    cont.resume(
                        UploadMediaResponse(
                            errorMessage = null,
                            media = uploadMediaResponse.media[0],
                            originalImage = image
                        )
                    )
                    
                }
            })
        
    }
    
    
    @Suppress("ConstantConditionIf")
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
                BLOCK_TEMPLATE_GALLERY_TOP.replace("%%MEDIA_ID_LIST%%", galleryImagesString) +
                // Images
                images.joinToString {
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
                    content = content,
                    tags = post.tags,
                    status = WPBlogPost.PublishStatus.DRAFT,
                    featuredImage = if (addingFeaturedImage) featuredImage.media!!.id.toString() else null
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
    
    
    companion object {
        
        private const val CLASSIC_TEMPLATE_SINGLE_IMAGE = """
            [gallery ids="%%MEDIA_ID%%" columns="1" size="large"]
            <p class="has-text-color has-small-font-size has-dark-gray-color">
                Published using <a href="https://play.google.com/store/apps/details?id=net.c306.photopress">PhotoPress for Android</a>
            </p>
        """
        
        private const val BLOCK_TEMPLATE_SINGLE_IMAGE = """
        <!-- wp:image {"id":%%MEDIA_ID%%,"align":"center","linkDestination":"media"} -->
        <div class="wp-block-image"><figure class="aligncenter"><a href="%%MEDIA_URL%%"><img src="%%MEDIA_LARGE%%" alt="%%MEDIA_ALT%%" class="wp-image-%%MEDIA_ID%%"/></a><figcaption>%%MEDIA_CAPTION%%</figcaption></figure></div>
        <!-- /wp:image -->
    """
        
        private const val CLASSIC_TEMPLATE_GALLERY = """
            [gallery ids="%%MEDIA_ID_LIST%%" columns="1" size="large"]
            %%POST_CAPTION%%
            <p class="has-text-color has-small-font-size has-dark-gray-color">
                Published using <a href="https://play.google.com/store/apps/details?id=net.c306.photopress">PhotoPress for Android</a>
            </p>
        """
        
        private const val BLOCK_TEMPLATE_GALLERY_TOP = """
        <!-- wp:gallery {"ids":[%%MEDIA_ID_LIST%%]} -->
            <figure class="wp-block-gallery columns-2 is-cropped">
            <ul class="blocks-gallery-grid">
    """
        private const val BLOCK_TEMPLATE_GALLERY_BOTTOM = """
            </ul>
            <figcaption class="blocks-gallery-caption">%%POST_CAPTION%%</figcaption>
        </figure>
<!-- /wp:gallery -->
    """
        private const val BLOCK_TEMPLATE_GALLERY_IMAGE =
            """<li class="blocks-gallery-item"><figure><img src="%%MEDIA_URL%%" alt="%%MEDIA_ALT%%" data-id="%%MEDIA_ID%%" class="wp-image-%%MEDIA_ID%%" /><figcaption class="blocks-gallery-item__caption">%%MEDIA_CAPTION%%</figcaption></figure></li>"""
        
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