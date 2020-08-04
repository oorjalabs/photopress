package net.c306.photopress.api

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import net.c306.photopress.database.UploadedMedia
import java.util.*

@Keep
data class WPMedia(
    
    /** The ID of the media item */
    @SerializedName("ID")
    val id: Int,
    
    /** The date the media was uploaded */
    val date: Date,
    
    /** ID of the post this media is attached to */
    @SerializedName("post_ID")
    val postId: Int? = null,
    
    /** ID of the user who uploaded the media */
    @SerializedName("author_ID")
    val authorId: Int? = null,
    
    /** URL to the file */
    @SerializedName("URL")
    val url: String,
    
    /** Unique identifier */
    val guid: String? = null,
    
    /** Filename */
    val file: String,
    
    /** File extension */
    val extension: String,
    
    /** File MIME type */
    @SerializedName("mime_type")
    val mimeType: String,
    
    /** Filename */
    val title: String,
    
    /** User-provided caption of the file */
    val caption: String?,
    
    /** Description of the file */
    val description: String?,
    
    /** Alternative text for image files. */
    val alt: String?,
    
    /** Media item thumbnail URL options */
    val thumbnails: Thumbnail?,
    
    /** Height of the media item (Image & video only) */
    val height: Int,
    
    /** Width of the media item (Image & video only) */
    val width: Int,
    
    /** Duration of the media item, in seconds (Video & audio only) */
    val length: Int? = null,
    
    /** Exif (meta) information about the media item (Image & audio only) */
    val exif: Exif
    
) {
    
    fun toUploadedMedia(): UploadedMedia {
        return UploadedMedia(
            id = id,
            url = url,
            guid = guid,
            file = file,
            extension = extension,
            mimeType = mimeType,
            title = title,
            caption = caption,
            description = description,
            alt = alt,
            thumbnails = thumbnails,
            height = height,
            width = width,
            exif = exif
        )
    }
    
    @Keep
    data class Thumbnail(
        val thumbnail: String,
        val medium: String,
        val large: String,
        @SerializedName("post-thumbnail")
        val postThumbnail: String
    )
    
    
    @Keep
    data class Exif(
        val aperture: Float, // 1.8,
        val credit: String, // "",
        val camera: String, // "Pixel 2 XL",
        val caption: String, // "",
        val created_timestamp: Long, // 0,
        val copyright: String, // "",
        val focal_length: Float, // 4.459,
        val iso: Int, // 50,
        val shutter_speed: Float, // 0.000427,
        val title: String, // "",
        val orientation: Int // 0/1
    )
    
    
    @Keep
    data class MediaAttributes(
        val title: String,
        val description: String,
        val caption: String,
        val alt: String? = null,
        val album: String? = null,
        @SerializedName("parent_id")
        val parentId: String? = null
    )
    
    
    @Keep
    data class UploadMediaResponse(
        val media: List<WPMedia>,
        val errors: List<String>?
    )
    
    
    companion object {
        const val FIELDS_STRING = "ID,date,URL,file,extension,mime_type,title,caption,description,alt,thumbnails,height,width,exif"
    }
    
}