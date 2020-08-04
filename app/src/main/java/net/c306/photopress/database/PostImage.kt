package net.c306.photopress.database

import android.net.Uri
import androidx.annotation.Keep
import androidx.room.*
import kotlin.random.Random

/**
 * Class stored to save image details.
 * Some properties are `var` so they can be updated using two-way data binding.
 */
@Keep
@Entity(
    tableName = "local_media",
    foreignKeys = [
        ForeignKey(
            entity = UploadedMedia::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("uploaded_media_id")
        ),
        ForeignKey(
            entity = PhotoPressPost::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("post_id")
        )
    ],
    indices = [Index(value = ["id"], unique = true)]
)
data class PostImage(
    
    @PrimaryKey
    val id: Int = generateId(),
    
    @ColumnInfo(name = "order")
    val order: Int,
    
    @ColumnInfo(name = "uri")
    val uri: Uri,
    
    @ColumnInfo(name = "caption")
    var caption: String? = null,
    
    @ColumnInfo(name = "alt_text")
    var altText: String? = null,
    
    @ColumnInfo(name = "description")
    var description: String? = null,
    
    @ColumnInfo(name = "name")
    var name: String? = null,
    
    @ColumnInfo(name = "file_details")
    val fileDetails: FileDetails? = null,
    
    @ColumnInfo(name = "uploaded_media_id")
    val uploadedMediaId: Int? = null,
    
    @ColumnInfo(name = "post_id")
    val postId: Int? = null
    
) {
    data class FileDetails(
        val fileName: String,
        val mimeType: String
    )
    
    companion object {
        fun generateId(): Int = Random.nextInt(0, Int.MAX_VALUE)
    }
}