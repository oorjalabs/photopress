package net.c306.photopress.database

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import net.c306.photopress.api.WPMedia

@Keep
@Entity(tableName = "uploaded_media", indices = [Index(value = ["id"], unique = true)])
data class UploadedMedia(
    
    /** The ID of the media item */
    @PrimaryKey
    val id: Int,
    
    /** URL to the file */
    @ColumnInfo(name = "url")
    val url: String,
    
    /** Unique identifier */
    @ColumnInfo(name = "guid")
    val guid: String? = null,
    
    /** Filename */
    @ColumnInfo(name = "file")
    val file: String,
    
    /** File extension */
    @ColumnInfo(name = "extension")
    val extension: String,
    
    /** File MIME type */
    @ColumnInfo(name = "mimeType")
    val mimeType: String,
    
    /** Filename */
    @ColumnInfo(name = "title")
    val title: String,
    
    /** User-provided caption of the file */
    @ColumnInfo(name = "caption")
    val caption: String?,
    
    /** Description of the file */
    @ColumnInfo(name = "description")
    val description: String?,
    
    /** Alternative text for image files. */
    @ColumnInfo(name = "alt")
    val alt: String?,
    
    /** Media item thumbnail URL options */
    @ColumnInfo(name = "thumbnails")
    val thumbnails: WPMedia.Thumbnail?,
    
    /** Height of the media item (Image & video only) */
    @ColumnInfo(name = "height")
    val height: Int,
    
    /** Width of the media item (Image & video only) */
    @ColumnInfo(name = "width")
    val width: Int,
    
    /** Exif (meta) information about the media item (Image & audio only) */
    @ColumnInfo(name = "exif")
    val exif: WPMedia.Exif
    
)