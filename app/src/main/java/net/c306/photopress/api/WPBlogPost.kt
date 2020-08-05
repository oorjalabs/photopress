package net.c306.photopress.api

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import java.util.*

//https://developer.wordpress.com/docs/api/1.1/post/sites/%24site/posts/new/
@Keep
data class WPBlogPost(
    @SerializedName("ID")
    val id: Int,
    val date: Date,
    val title: String,
    @SerializedName("URL")
    val url: String,
    @SerializedName("short_URL")
    val shortUrl: String,
    val status: PublishStatus? = PublishStatus.PUBLISH,
    @SerializedName("post_thumbnail")
    val postThumbnail: Thumbnail,
    val format: PostFormat? = PostFormat.DEFAULT,
    val tags: Map<String, WPTag>,
    val categories: Map<String, WPCategory>
) {
    
    @Keep
    data class Thumbnail(
        @SerializedName("ID")
        val id: Int,
        @SerializedName("URL")
        val url: String,
        @SerializedName("mime_type")
        val mimeType: String,
        val width: Int,
        val height: Int
    )
    
    @Keep
    enum class PublishStatus (val value: String) {
        
        /** (default) Publish the post. */
        @SerializedName("publish")
        PUBLISH("publish"),
        
        /** Privately publish the post. */
        @SerializedName("private")
        PRIVATE("private"),
        
        /** Save the post as a draft. */
        @SerializedName("draft")
        DRAFT("draft"),
        
        /** Mark the post as pending editorial approval. */
        @SerializedName("pending")
        PENDING("pending"),
        
        /** Schedule the post (alias for publish; you must also set a future date). */
        @SerializedName("future")
        FUTURE("future"),
        
        /** Save a placeholder for a newly created post, with no content. */
        @SerializedName("auto-draft")
        AUTO_DRAFT("auto-draft")
    }
    
    @Keep
    data class UpdatePostStatusRequest(
        val status: PublishStatus,
        val date: String? = null
    )
    
    @Keep
    enum class PostFormat (val value: String) {
        /** (default) Use default post format */
        @SerializedName("default")
        DEFAULT("default"),
        @SerializedName("standard")
        STANDARD("standard"),
        @SerializedName("image")
        IMAGE("image"),
        @SerializedName("gallery")
        GALLERY("gallery")
    }
    
    //https://developer.wordpress.com/docs/api/1.1/post/sites/%24site/posts/new/
    @Keep
    data class CreatePostRequest(
            val title: String,
            val content: String?,
            val status: PublishStatus? = PublishStatus.PUBLISH,
            /** List of tags (name or id) **/
            val tags: List<String>? = null,
            val format: PostFormat? = PostFormat.STANDARD,
            @SerializedName("featured_image")
            val featuredImage: String? = null,
            @SerializedName("media_urls")
            val mediaUrls: List<String>? = null,
            @SerializedName("media_attrs")
            val mediaAttrs: List<WPMedia.MediaAttributes>? = null
                                ) {
    }
    
    companion object {
        const val FIELDS_STRING = "ID,date,title,URL,short_URL,status,post_thumbnail,format,tags,categories"
    }
}