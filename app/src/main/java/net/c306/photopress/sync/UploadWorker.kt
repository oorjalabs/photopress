package net.c306.photopress.sync

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.work.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.c306.photopress.api.ApiClient
import net.c306.photopress.api.WPMedia
import net.c306.photopress.database.AppDatabase
import net.c306.photopress.database.PostImage
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.min

class UploadWorker(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {
    
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        
        val postsDb = AppDatabase.getPostsInstance(applicationContext)
        
        // Get upload pending posts from database
        val pendingPosts = postsDb.getAllPending()
        
        // If not pending posts, return
        if (pendingPosts.isEmpty()) return@withContext Result.success()
        
        val localImagesDb = AppDatabase.getLocalImagesInstance(applicationContext)
        val uploadedImagesDb = AppDatabase.getUploadedImagesInstance(applicationContext)
        
//        pendingPosts.forEach { pendingPost ->
//
//            // Get local images for pending posts from database and upload them
//            val uploadedImages = pendingPost.postImages
//                .filter { it.uploadedImageId != null || it.localImageId != null }
//                .map {
//                    it.localImageId?.let { localImageId ->
//                        // Get local image from database
//                        val localImage = localImagesDb.getById(localImageId) ?: return@map null
//
//                        // Upload image to server
//                        val uploadImageResult = uploadImage(pendingPost.blogId, localImage)
//
//                        if (uploadImageResult.media == null) {
//                            // Error uploading image
//                            Timber.w("Error uploading image: ${uploadImageResult.errorMessage}")
//                            return@map null
//                        }
//
//                        // Save uploaded media details to database
//                        val uploadedMedia = uploadImageResult.media.toUploadedMedia()
//                        uploadedImagesDb.insert(uploadedMedia)
//
//                        Pair(it, uploadedMedia.id)
//                    }
//                    ?: Pair(it, it.uploadedImageId)
//                }
//
//
//            // If error uploading some images, write post back to database (in case some of images were updated) then return with error/retry
//            val hadFailedUploads = uploadedImages.any { it == null }
//            if (hadFailedUploads) {
//
//                val updatedImages = pendingPost.postImages.map { image ->
//
//                    // Get image id if image was uploaded successfully
//                    val uploadedImageId = uploadedImages.find {
//                        image.localImageId != null && it?.first?.localImageId == image.localImageId
//                    }?.second
//
//                    // Return uploaded image or original image
//                    uploadedImageId?.let {
//                        PhotoPressPost.PhotoPostImage(
//                            image.order,
//                            uploadedImageId = uploadedImageId
//                        )
//                    } ?: image
//                }
//
//                // Update post in db with updated images
//                postsDb.update(pendingPost.copy(postImages = updatedImages))
//
//                return@forEach
//            }
//
//
//            // TODO: 04/08/2020 Create and upload post content
//
//
//        }
        
        return@withContext Result.success()
    }
    
    private data class UploadMediaResponse(
        val media: WPMedia? = null,
        val errorMessage: String? = null
    )
    
    
    private suspend fun uploadImage(
        blogId: Int,
        postImage: PostImage
    ) = suspendCoroutine<UploadMediaResponse> { cont ->
        
        val imageDetails = getFileForUri(postImage)
        
        if (imageDetails == null) {
            Timber.w("File not found!: $postImage")
            return@suspendCoroutine
        }
        
        val (file, mimeType) = imageDetails
        
        
        val imageBody = file.asRequestBody(mimeType.toMediaType())
        
        val filePart: MultipartBody.Part = MultipartBody.Part.createFormData(
            "media[0]",
            postImage.name ?: file.name,
            imageBody
        )
        
        val captionAttr = MultipartBody.Part.createFormData(
            "attrs[0][caption]",
            postImage.caption ?: ""
        )
        val titleAttr = MultipartBody.Part.createFormData(
            "attrs[0][title]",
            postImage.name ?: file.nameWithoutExtension
        )
        val altAttr = MultipartBody.Part.createFormData(
            "attrs[0][alt]",
            postImage.altText ?: postImage.name ?: file.nameWithoutExtension
        )
        val descriptionAttr = MultipartBody.Part.createFormData(
            "attrs[0][description]",
            postImage.description ?: ""
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
    
    
    /**
     * Takes a content URI and writes the contents to a file in app's storage.
     * Returns the app's copy of file and its mime type
     * @return Pair with file and its mime-type, or null if the uri couldn't be read
     */
    private fun getFileForUri(postImage: PostImage): Pair<File, String>? {
        
        if (postImage.fileDetails == null) return null
        
        // Open a specific media item using ParcelFileDescriptor.
        val resolver = applicationContext.contentResolver
        
        // Open selected file as input stream
        resolver.openInputStream(postImage.uri)?.use { stream ->
            // Write it to app's storage as file
            val imageFile = File(applicationContext.filesDir, postImage.fileDetails.fileName)
            imageFile.outputStream().use {
                stream.copyTo(it)
            }
            return Pair(imageFile, postImage.fileDetails.mimeType)
        }
        
        return null
    }
    
    
    companion object {
        
        internal const val ARG_SYNC_RESPONSE_MESSAGE = "sync_response_message"
        internal const val ARG_SYNC_CHANGES_COUNT = "sync_changes_count"
        
        private const val WORK_ONE_TIME_SYNC = "work_one_time_sync"
        private const val WORK_PERIODIC_SYNC = "work_periodic_sync"
        
        /**
         * Do a one time manual sync
         */
        internal fun sync(
            workerTag: String,
            delayInSeconds: Long = 0L,
            observerOwner: LifecycleOwner? = null,
            observer: Observer<WorkInfo>? = null
        ) {
            
            // Use WorkManager to execute work now
            val workManager = WorkManager.getInstance()
            
            val syncConstraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            
            
            // Sync all/selected folders
            val primarySync = OneTimeWorkRequestBuilder<UploadWorker>()
                .setConstraints(syncConstraints)
                .addTag(WORK_ONE_TIME_SYNC)
                .addTag(workerTag)
                .setInputData(workDataOf())
                .setInitialDelay(delayInSeconds, TimeUnit.SECONDS)
                .build()
            
            // Enqueue work
            workManager
                .beginUniqueWork(WORK_ONE_TIME_SYNC, ExistingWorkPolicy.REPLACE, primarySync)
                .enqueue()
            
            // Let callers track progress
            if (observerOwner != null && observer != null) {
                workManager.getWorkInfoByIdLiveData(primarySync.id).observe(observerOwner, observer)
            }
        }
        
        
        internal fun cancelSync(workerTag: String) {
            WorkManager.getInstance().cancelAllWorkByTag(workerTag)
        }
        
        /**
         * Schedule a periodic sync, or update schedule
         */
        internal fun scheduleSync(context: Context, intervalInMins: Int) {
            
            // Use WorkManager to execute work now
            
            WorkManager.getInstance().run {
                
                // If sync frequency is 'never', cancel work, don't start new
                if (intervalInMins <= 0) {
                    cancelUniqueWork(WORK_PERIODIC_SYNC)
                    return
                }
                
                // Require network to run
                val syncConstraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
                
                // Create request, set tag and input, set schedule
                val syncRequest = PeriodicWorkRequestBuilder<UploadWorker>(
                    intervalInMins.toLong(),
                    TimeUnit.MINUTES,
                    PeriodicWorkRequest.MIN_PERIODIC_FLEX_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                    .setConstraints(syncConstraints)
                    .setInputData(workDataOf())
                    .addTag(WORK_PERIODIC_SYNC)
                    .build()
                
                
                // Enqueue background sync with name - used for cancelling
                enqueueUniquePeriodicWork(
                    WORK_PERIODIC_SYNC,
                    ExistingPeriodicWorkPolicy.REPLACE,
                    syncRequest
                )
                
            }
            
        }
        
        /**
         * Trim message length at 1000 chars
         */
        internal fun trimMessage(message: String): String {
            return message.substring(0, min(1000, message.length))
        }
        
    }
}