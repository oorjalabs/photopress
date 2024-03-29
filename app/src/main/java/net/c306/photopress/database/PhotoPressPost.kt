package net.c306.photopress.database

import androidx.annotation.Keep
import androidx.room.*
import com.google.gson.annotations.SerializedName
import net.c306.photopress.api.WPBlogPost
import net.c306.photopress.utils.Json
import java.util.*

//https://developer.wordpress.com/docs/api/1.1/post/sites/%24site/posts/new/
@Keep
@Entity(
    tableName = "blog_posts",
    foreignKeys = [ForeignKey(
        entity = PostImage::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("post_thumbnail")
    )],
    indices = [Index(value = ["id"], unique = true)]
)
data class PhotoPressPost(
    
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    @ColumnInfo(name = "blog_id")
    val blogId: Int,
    
    @ColumnInfo(name = "timestamp")
    val timestamp: Long = Date().time,
    
    @ColumnInfo(name = "title")
    val title: String,
    
    @ColumnInfo(name = "post_caption")
    val postCaption: String,
    
    @ColumnInfo(name = "tags")
    val tags: List<String> = emptyList(),
    
    @ColumnInfo(name = "categories")
    val categories: List<String> = emptyList(),
    
    @ColumnInfo(name = "post_thumbnail")
    val postThumbnail: Int,
    
    @ColumnInfo(name = "status")
    val status: PhotoPostStatus = PhotoPostStatus.DRAFT,
    
    @ColumnInfo(name = "scheduled_time")
    val scheduledTime: Long? = null,
    
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
                return Json.getInstance().fromJson(storageString, PhotoPostImage::class.java)
            }
        }
    }
    
    @Keep
    enum class PhotoPostStatus{
        DRAFT,
        PUBLISH,
        SCHEDULE
    }
}