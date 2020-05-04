package net.c306.photopress.api

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

//https://developer.wordpress.com/docs/api/1.1/post/sites/%24site/posts/new/
@Keep
data class BlogPostRequest(
    val title: String,
    val content: String?,
    val status: WPBlogPost.PublishStatus? = WPBlogPost.PublishStatus.PUBLISH,
    val tags: List<String>? = null,
    val format: WPBlogPost.PostFormat? = WPBlogPost.PostFormat.STANDARD,
    @SerializedName("featured_image")
    val featuredImage: String? = null,
    @SerializedName("media_urls")
    val mediaUrls: List<String>? = null,
    @SerializedName("media_attrs")
    val mediaAttrs: List<WPBlogPost.MediaAttributes>? = null
) {
    
    companion object {
        const val FIELDS_STRING = "ID,date,title,URL,short_URL,status,post_thumbnail,format"
    }
}