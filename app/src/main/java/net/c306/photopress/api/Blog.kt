package net.c306.photopress.api

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import net.c306.photopress.utils.Json

@Keep
data class Blog(
    @SerializedName("ID")
    val id: Int,
    val name: String,
    val description: String,
    @SerializedName("URL")
    val url: String,
    val jetpack: Boolean,
    val icon: BlogIcon,
    val capabilities: BlogCapabilities,
    val quota: BlogQuota,
    val options: BlogOptions
) {
    
    fun toJson(): String {
        return Json.getInstance().toJson(this)
    }
    
    @Keep
    data class BlogIcon(
        val img: String,
        val ico: String,
        @SerializedName("media_id")
        val mediaId: Int
    )
    
    @Keep
    data class BlogCapabilities(
        @SerializedName("upload_files")
        val uploadFiles: Boolean,
        @SerializedName("publish_posts")
        val publishPosts: Boolean
    )
    
    @Keep
    data class BlogQuota(
        @SerializedName("percent_used")
        val percentUsed: Double,
        @SerializedName("space_available")
        val spaceAvailable: Long
    )
    
    @Keep
    data class BlogOptions(
        @SerializedName("featured_images_enabled")
        val featuredImagesEnabled: Boolean,
        @SerializedName("default_category")
        val defaultCategory: Int
    )
    
    @Keep
    data class GetSitesResponse(
        val sites: List<Blog>
    )
    
    
    companion object {
        const val FIELDS_STRING = "ID,name,description,URL,jetpack,icon,capabilities,quota"
        const val OPTIONS_STRING = "featured_images_enabled,default_category"
        
        fun fromJson(jsonString: String): Blog {
            return Json.getInstance().fromJson(jsonString, Blog::class.java)
        }
    }
}