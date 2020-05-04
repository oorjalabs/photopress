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
    fun listBlogs(@Query(ApiConstants.ARG_FIELDS) fields: String?, @Query(ApiConstants.ARG_OPTIONS) options: String?): Call<SitesResponse>


//    @POST(ApiConstants.CREATE_POST)
//    @Multipart
//    fun createBlogpost(
//        @Path(ApiConstants.ARG_BLOG_ID) blogId: String,
//        @Query(ApiConstants.ARG_FIELDS) fields: String?,
//        @Part title: MultipartBody.Part,
//        @Part content: MultipartBody.Part,
////        @Part status: MultipartBody.Part,
//        @Part("status") status: String = "publish",
//        @Part media: MultipartBody.Part
//    ): Call<WPBlogPost>
    
    
    @POST(ApiConstants.CREATE_POST)
    fun uploadBlogpost(
        @Path(ApiConstants.ARG_BLOG_ID) blogId: String,
        @Query(ApiConstants.ARG_FIELDS) fields: String?,
        @Body body: BlogPostRequest
    ): Call<WPBlogPost>
    
    
    @POST(ApiConstants.UPDATE_POST)
    fun updatePostStatus(
        @Path(ApiConstants.ARG_BLOG_ID) blogId: String,
        @Path(ApiConstants.ARG_POST_ID) postId: String,
        @Query(ApiConstants.ARG_FIELDS) fields: String?,
        @Body body: UpdatePostStatusRequest
    ): Call<WPBlogPost>
    
    
    @POST(ApiConstants.UPLOAD_MEDIA)
    @Multipart
    fun uploadMedia(
        @Path(ApiConstants.ARG_BLOG_ID) blogId: String,
        @Query(ApiConstants.ARG_FIELDS) fields: String?,
        @Part media: MultipartBody.Part,
        @Part("attrs") attrs: List<WPBlogPost.MediaAttributes>
    ): Call<UploadMediaResponse>
    
    
    @Keep
    data class UpdatePostStatusRequest(
        val status: WPBlogPost.PublishStatus
    )
    
    @Keep
    data class UploadMediaResponse(
        val media: List<WPMedia>,
        val errors: List<String>?
    )
    
    @Keep
    data class SitesResponse(
        val sites: List<Blog>
    )

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
