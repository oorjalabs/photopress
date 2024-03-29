package net.c306.photopress.api

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import net.c306.photopress.BuildConfig
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Interface for defining REST request functions
 */
internal interface WpService {

    @POST(ApiConstants.TOKEN_URL)
    @FormUrlEncoded
    suspend fun getToken(@FieldMap fields: Map<String, String>): GetTokenResponse

    @GET(ApiConstants.VALIDATE_URL)
    suspend fun validateToken(
        @Query(ApiConstants.ARG_CLIENT_ID) clientId: String = BuildConfig.WP_ID,
        @Query(ApiConstants.ARG_TOKEN) token: String
    ): ValidateTokenResponse

    @GET(ApiConstants.ABOUT_ME_URL)
    suspend fun aboutMe(@Query(ApiConstants.ARG_FIELDS) fields: String?): UserDetails

    @GET(ApiConstants.BLOG_LIST)
    suspend fun listBlogs(
        @Query(ApiConstants.ARG_FIELDS) fields: String?,
        @Query(ApiConstants.ARG_OPTIONS) options: String?
    ): Blog.GetSitesResponse

    @GET(ApiConstants.GET_TAGS_FOR_SITE)
    suspend fun getTagsForSite(
        @Path(ApiConstants.ARG_BLOG_ID) blogId: String,
        @Query(WPTag.ARG_ORDER_BY) orderBy: String = WPTag.VALUE_ORDER_BY,
        @Query(WPTag.ARG_ORDER) order: String = WPTag.VALUE_ORDER,
        @Query(WPTag.ARG_NUMBER) number: Number = WPTag.VALUE_NUMBER,
        @Query(WPTag.ARG_FIELDS) fields: String = WPTag.FIELDS_STRING
    ): WPTag.TagsResponse

    @GET(ApiConstants.GET_CATEGORIES_FOR_SITE)
    suspend fun getCategoriesForSite(
        @Path(ApiConstants.ARG_BLOG_ID) blogId: String,
        @Query(WPCategory.ARG_ORDER_BY) orderBy: String = WPCategory.VALUE_ORDER_BY,
        @Query(WPCategory.ARG_ORDER) order: String = WPCategory.VALUE_ORDER,
        @Query(WPCategory.ARG_NUMBER) number: Number = WPCategory.VALUE_NUMBER,
        @Query(WPCategory.ARG_FIELDS) fields: String = WPCategory.FIELDS_STRING
    ): WPCategory.GetCategoriesResponse

    @POST(ApiConstants.CREATE_CATEGORY)
    @FormUrlEncoded
    suspend fun addCategory(
        @Path(ApiConstants.ARG_BLOG_ID) blogId: String,
        @Query(ApiConstants.ARG_FIELDS) fields: String = WPCategory.FIELDS_STRING,
        @FieldMap request: Map<String, String>
    ): WPCategory

    @POST(ApiConstants.CREATE_POST)
    suspend fun uploadBlogpost(
        @Path(ApiConstants.ARG_BLOG_ID) blogId: String,
        @Query(ApiConstants.ARG_FIELDS) fields: String?,
        @Body body: WPBlogPost.CreatePostRequest
    ): WPBlogPost

    @POST(ApiConstants.UPDATE_POST)
    suspend fun updatePostStatus(
        @Path(ApiConstants.ARG_BLOG_ID) blogId: String,
        @Path(ApiConstants.ARG_POST_ID) postId: String,
        @Query(ApiConstants.ARG_FIELDS) fields: String?,
        @Body body: WPBlogPost.UpdatePostStatusRequest
    ): WPBlogPost

    @POST(ApiConstants.UPLOAD_MEDIA)
    suspend fun uploadSingleMedia(
        @Path(ApiConstants.ARG_BLOG_ID) blogId: String,
        @Query(ApiConstants.ARG_FIELDS) fields: String?,
        @Body contents: MultipartBody
    ): WPMedia.UploadMediaResponse

    @POST(ApiConstants.UPLOAD_MEDIA)
    suspend fun uploadMediaMulti(
        @Path(ApiConstants.ARG_BLOG_ID) blogId: String,
        @Query(ApiConstants.ARG_FIELDS) fields: String?,
        @Body contents: MultipartBody
    ): WPMedia.UploadMediaResponse

    @POST(ApiConstants.UPDATE_MEDIA_ATTRIBUTES)
    suspend fun updateMediaAttributes(
        @Path(ApiConstants.ARG_BLOG_ID) blogId: String,
        @Path(ApiConstants.ARG_MEDIA_ID) mediaId: String,
        @Query(ApiConstants.ARG_FIELDS) fields: String?,
        @Body body: WPMedia.UpdateMediaAttributesRequest
    ): WPMedia

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