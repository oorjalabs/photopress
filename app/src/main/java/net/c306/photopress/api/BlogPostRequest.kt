package net.c306.photopress.api

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

//https://developer.wordpress.com/docs/api/1.1/post/sites/%24site/posts/new/
@Keep
data class BlogPostRequest(
    val title: String,
    val content: String?,
    val status: PublishStatus? = PublishStatus.PUBLISH,
    val tags: List<String>? = null,
    val format: PostFormat? = PostFormat.DEFAULT,
    @SerializedName("featured_image")
    val featuredImage: String? = null,
    //  val media: MultipartBody.Part,
    @SerializedName("media_urls")
    val mediaUrls: List<String>? = null,
    @SerializedName("media_attrs")
    val mediaAttrs: List<MediaAttributes>? = null
) {
    enum class PublishStatus {
        @SerializedName("publish")
        PUBLISH,
        @SerializedName("private")
        PRIVATE,
        @SerializedName("draft")
        DRAFT,
        @SerializedName("pending")
        PENDING,
        @SerializedName("future")
        FUTURE,
        @SerializedName("auto-draft")
        AUTO_DRAFT
    }


    enum class PostFormat {
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
        val caption: String
    )

    companion object {
        const val FIELDS_STRING = "id,date,title,URL,short_URL,status,post_thumbnail,format"
    }
}