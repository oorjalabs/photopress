package net.c306.photopress.database

import android.net.Uri
import androidx.room.TypeConverter
import com.google.gson.reflect.TypeToken
import net.c306.photopress.api.WPBlogPost
import net.c306.photopress.api.WPMedia
import net.c306.photopress.utils.Json
import java.lang.reflect.Type


class TypeConverters {
    
    @TypeConverter
    fun fromPostImagesListToString(items: List<PhotoPressPost.PhotoPostImage>): String =
        items.joinToString("\n") { it.toStorageString() }
    
    @TypeConverter
    fun fromStringToPostImagesList(value: String): List<PhotoPressPost.PhotoPostImage> =
        value
            .split("\n")
            .map { PhotoPressPost.PhotoPostImage.fromStorageString(it) }
    
    @TypeConverter
    fun fromPhotoPostImageToString(image: PhotoPressPost.PhotoPostImage): String =
        image.toStorageString()
    
    @TypeConverter
    fun fromStringToPhotoPostImage(storageString: String): PhotoPressPost.PhotoPostImage =
        PhotoPressPost.PhotoPostImage.fromStorageString(storageString)
    
    @TypeConverter
    fun fromPhotoPostStatusToString(status: PhotoPressPost.PhotoPostStatus): String = status.name
    
    @TypeConverter
    fun fromStringToPhotoPostStatus(statusName: String): PhotoPressPost.PhotoPostStatus =
        PhotoPressPost.PhotoPostStatus.valueOf(statusName)
    
    @TypeConverter
    fun fromPhotoPostFormatToString(format: WPBlogPost.PostFormat): String = format.name
    
    @TypeConverter
    fun fromStringToPhotoPostFormat(formatName: String): WPBlogPost.PostFormat =
        WPBlogPost.PostFormat.valueOf(formatName)
    
    @TypeConverter
    fun fromStringListToString(list: List<String>): String = Json.getInstance().toJson(list)
    
    @TypeConverter
    fun fromStringToStringList(listString: String): List<String> {
        val listType: Type = object : TypeToken<List<String?>?>() {}.type
        return Json.getInstance().fromJson(listString, listType)
    }
    
    @TypeConverter
    fun fromThumbnailToString(thumbnail: WPMedia.Thumbnail): String =
        Json.getInstance().toJson(thumbnail)
    
    @TypeConverter
    fun fromStringToThumbnail(thumbnailString: String): WPMedia.Thumbnail =
        Json.getInstance().fromJson(thumbnailString, WPMedia.Thumbnail::class.java)
    
    @TypeConverter
    fun fromExifToString(exif: WPMedia.Exif): String =
        Json.getInstance().toJson(exif)
    
    @TypeConverter
    fun fromStringToExif(exifString: String): WPMedia.Exif =
        Json.getInstance().fromJson(exifString, WPMedia.Exif::class.java)
    
    @TypeConverter
    fun fromUriToString(uri: Uri): String =
        Json.getInstance().toJson(uri)
    
    @TypeConverter
    fun fromStringToUri(uriString: String): Uri =
        Json.getInstance().fromJson(uriString, Uri::class.java)
    
    @TypeConverter
    fun fromFileDetailsToString(fileDetails: PostImage.FileDetails): String =
        Json.getInstance().toJson(fileDetails)
    
    @TypeConverter
    fun fromStringToFileDetails(fileDetailsString: String): PostImage.FileDetails =
        Json.getInstance().fromJson(fileDetailsString, PostImage.FileDetails::class.java)
}