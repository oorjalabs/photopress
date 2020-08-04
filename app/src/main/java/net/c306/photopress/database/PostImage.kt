package net.c306.photopress.database

import android.net.Uri
import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlin.random.Random

/**
 * Class stored to save image details.
 * Some properties are `var` so they can be updated using two-way data binding.
 */
@Keep
@Entity(tableName = "local_media", indices = [Index(value = ["id"], unique = true)])
data class PostImage(
    
    @PrimaryKey
    val id: Int = generateId(),
    
    @ColumnInfo(name = "uri")
    val uri: Uri,
    
    @ColumnInfo(name = "caption")
    var caption: String? = null,
    
    @ColumnInfo(name = "altText")
    var altText: String? = null,
    
    @ColumnInfo(name = "description")
    var description: String? = null,
    
    @ColumnInfo(name = "name")
    var name: String? = null,
    
    @ColumnInfo(name = "fileDetails")
    val fileDetails: FileDetails? = null
    
) {
    data class FileDetails(
        val fileName: String,
        val mimeType: String
    )
    
    companion object {
        fun generateId(): Int = Random.nextInt(0, Int.MAX_VALUE)
    }
}