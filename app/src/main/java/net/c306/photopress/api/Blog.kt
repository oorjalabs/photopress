package net.c306.photopress.api

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

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
        return Gson().toJson(this)
    }

    data class BlogIcon(
        val img: String,
        val ico: String,
        @SerializedName("media_id")
        val mediaId: Int
    )

    data class BlogCapabilities(
        @SerializedName("upload_files")
        val uploadFiles: Boolean,
        @SerializedName("publish_posts")
        val publishPosts: Boolean
    )

    data class BlogQuota(
        @SerializedName("percent_used")
        val percentUsed: Double,
        @SerializedName("space_available")
        val spaceAvailable: Long
    )

    data class BlogOptions(
        @SerializedName("featured_images_enabled")
        val featuredImagesEnabled: Boolean,
        @SerializedName("default_category")
        val defaultCategory: Int
    )

    companion object {
        const val BLOG_FIELDS = "ID,name,description,URL,jetpack,icon,capabilities,quota"
        const val OPTIONS_FIELDS = "featured_images_enabled,default_category"

        fun fromJson(jsonString: String): Blog {
            return Gson().fromJson(jsonString, Blog::class.java)
        }
    }
}