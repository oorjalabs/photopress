package net.c306.photopress.api

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import java.util.*

//https://developer.wordpress.com/docs/api/1.1/post/sites/%24site/posts/new/
@Keep
data class BlogPostResponse(
    val id: Int,
    val date: Date,
    val title: String,
    @SerializedName("URL")
    val url: String,
    @SerializedName("short_URL")
    val shortUrl: String,
    val status: BlogPostRequest.PublishStatus? = BlogPostRequest.PublishStatus.PUBLISH,
    @SerializedName("post_thumbnail")
    val postThumbnail: Thumbnail,
    val format: BlogPostRequest.PostFormat? = BlogPostRequest.PostFormat.DEFAULT
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
}