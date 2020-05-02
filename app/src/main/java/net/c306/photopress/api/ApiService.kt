package net.c306.photopress.api

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import net.c306.photopress.BuildConfig
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
