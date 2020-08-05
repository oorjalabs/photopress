package net.c306.photopress.api

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import net.c306.photopress.BuildConfig
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

/**
 * Interface for defining REST request functions
 */
interface ApiService {


    @POST(ApiConstants.TOKEN_URL)
    @FormUrlEncoded
    fun getToken(@FieldMap fields: Map<String, String>): Call<GetTokenResponse>


    @GET(ApiConstants.VALIDATE_URL)
    fun validateToken(
        @Query(ApiConstants.ARG_CLIENT_ID) clientId: String = BuildConfig.WP_ID,
        @Query(ApiConstants.ARG_TOKEN) token: String
    ): Call<ValidateTokenResponse>


    @GET(ApiConstants.ABOUT_ME_URL)
    fun aboutMe(@Query(ApiConstants.ARG_FIELDS) fields: String?): Call<UserDetails>

    
    @GET(ApiConstants.BLOG_LIST)
    fun listBlogs(
        @Query(ApiConstants.ARG_FIELDS) fields: String?,
        @Query(ApiConstants.ARG_OPTIONS) options: String?
    ): Call<Blog.GetSitesResponse>
    
    
    @GET(ApiConstants.GET_TAGS_FOR_SITE)
    fun getTagsForSite(
            @Path(ApiConstants.ARG_BLOG_ID) blogId: String,
            @Query(WPTag.ARG_ORDER_BY) orderBy: String = WPTag.VALUE_ORDER_BY,
            @Query(WPTag.ARG_ORDER) order: String = WPTag.VALUE_ORDER,
            @Query(WPTag.ARG_NUMBER) number: Number = WPTag.VALUE_NUMBER,
            @Query(WPTag.ARG_FIELDS) fields: String = WPTag.FIELDS_STRING
    ): Call<WPTag.TagsResponse>
    
    
    @POST(ApiConstants.CREATE_POST)
    fun uploadBlogpost(
        @Path(ApiConstants.ARG_BLOG_ID) blogId: String,
        @Query(ApiConstants.ARG_FIELDS) fields: String?,
        @Body body: WPBlogPost.CreatePostRequest
    ): Call<WPBlogPost>
    
    
    @POST(ApiConstants.UPDATE_POST)
    fun updatePostStatus(
        @Path(ApiConstants.ARG_BLOG_ID) blogId: String,
        @Path(ApiConstants.ARG_POST_ID) postId: String,
        @Query(ApiConstants.ARG_FIELDS) fields: String?,
        @Body body: WPBlogPost.UpdatePostStatusRequest
    ): Call<WPBlogPost>
    
    
    @POST(ApiConstants.UPLOAD_MEDIA)
    @Multipart
    fun uploadMedia(
        @Path(ApiConstants.ARG_BLOG_ID) blogId: String,
        @Query(ApiConstants.ARG_FIELDS) fields: String?,
        @Part media: MultipartBody.Part,
        @Part caption: MultipartBody.Part,
        @Part title: MultipartBody.Part,
        @Part alt: MultipartBody.Part,
        @Part description: MultipartBody.Part
    ): Call<WPMedia.UploadMediaResponse>
    
    
    @POST(ApiConstants.UPLOAD_MEDIA)
    fun uploadMediaMulti(
        @Path(ApiConstants.ARG_BLOG_ID) blogId: String,
        @Query(ApiConstants.ARG_FIELDS) fields: String?,
        @Body contents: MultipartBody
    ): Call<WPMedia.UploadMediaResponse>
    
    
    @POST(ApiConstants.UPDATE_MEDIA_ATTRIBUTES)
    fun updateMediaAttributes(
        @Path(ApiConstants.ARG_BLOG_ID) blogId: String,
        @Path(ApiConstants.ARG_MEDIA_ID) mediaId: String,
        @Query(ApiConstants.ARG_FIELDS) fields: String?,
        @Body body: WPMedia.UpdateMediaAttributesRequest
    ): Call<WPMedia>
    
    @Keep
    data class ValidateTokenResponse(
        @SerializedName(ApiConstants.ARG_CLIENT_ID)
        val clientId: String?,
        @SerializedName(ApiConstants.ARG_USER_ID)
        val userId: String?,
        @SerializedName(ApiConstants.ARG_BLOG_ID)
        val blogId: String?,
        val scope: String?,
        val error: String?
    )

    @Keep
    data class GetTokenResponse(
        
        @SerializedName(ApiConstants.ARG_ACCESS_TOKEN)
        val accessToken: String?,

        @SerializedName(ApiConstants.ARG_BLOG_ID)
        val blogId: String?,

        @SerializedName(ApiConstants.ARG_BLOG_URL)
        val blogUrl: String?,

        @SerializedName(ApiConstants.ARG_TOKEN_TYPE)
        val tokenType: String?,

        val error: String?

    )

}
