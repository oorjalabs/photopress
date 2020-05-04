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
    val format: PostFormat? = PostFormat.DEFAULT
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
    enum class PostFormat {
        
        /** (default) Use default post format */
        @SerializedName("default")
        DEFAULT,
        
        @SerializedName("standard")
        STANDARD,
        
        @SerializedName("image")
        IMAGE,
        
        @SerializedName("gallery")
        GALLERY
    }
    
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
}