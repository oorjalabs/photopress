package net.c306.photopress.database

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import net.c306.photopress.api.WPBlogPost
import net.c306.photopress.utils.Json
import java.util.*

//https://developer.wordpress.com/docs/api/1.1/post/sites/%24site/posts/new/
@Keep
@Entity(tableName = "blog_posts", indices = [Index(value = ["id"], unique = true)])
data class PhotoPressPost(
    
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    @ColumnInfo(name = "blog_id")
    val blogId: Int,
    
    @ColumnInfo(name = "timestamp")
    val timestamp: Long = Date().time,
    
    @ColumnInfo(name = "title")
    val title: String,
    
    @ColumnInfo(name = "content")
    val content: String,
    
    @ColumnInfo(name = "tags")
    val tags: List<String> = emptyList(),
    
    @ColumnInfo(name = "categories")
    val categories: List<String> = emptyList(),
    
    @ColumnInfo(name = "post_images")
    val postImages: List<PhotoPostImage>,
    
    @ColumnInfo(name = "post_thumbnail")
    val postThumbnail: PhotoPostImage,
    
    @ColumnInfo(name = "status")
    val status: PhotoPostStatus = PhotoPostStatus.PUBLISH,
    
    @ColumnInfo(name = "format")
    val format: WPBlogPost.PostFormat = WPBlogPost.PostFormat.DEFAULT,
    
    @ColumnInfo(name = "upload_pending")
    val uploadPending: Boolean = false // Make true when post is ready to be uploaded

) {
    
    @Keep
    data class PhotoPostImage(
        val order: Int,
        // Id of image in PostImage/local_media table
        @SerializedName("local_image_id")
        val localImageId: Int? = null,
        // Id of image in UploadedMedia/uploaded_media table
        @SerializedName("uploaded_image_id")
        val uploadedImageId: Int? = null
    ) {
        fun toStorageString(): String {
            return Json.getInstance().toJson(this)
        }
        
        companion object {
            fun fromStorageString(storageString: String): PhotoPostImage {
                return Json.getInstance().fromJson<PhotoPostImage>(storageString, PhotoPostImage::class.java)
            }
        }
    }
    
    @Keep
    enum class PhotoPostStatus{
        LOCAL_DRAFT,
        DRAFT,
        PUBLISH,
        SCHEDULE
    }
}