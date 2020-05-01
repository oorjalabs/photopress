package net.c306.photopress.api

import com.google.gson.annotations.SerializedName
import net.c306.photopress.BuildConfig

data class TokenRequest(

    @SerializedName(value = ApiConstants.ARG_CLIENT_ID)
    val clientId: String = BuildConfig.WP_ID,

    @SerializedName(value = ApiConstants.ARG_REDIRECT_URI)
    val redirectUri: String = ApiConstants.AUTH_REDIRECT_URL,

    @SerializedName(value = ApiConstants.ARG_CLIENT_SECRET)
    val clientSecret: String = BuildConfig.WP_SECRET,

    val code: String,

    @SerializedName(value = ApiConstants.ARG_GRANT_TYPE)
    val grantType: String = "authorization_code"
) {
    fun toFieldMap(): Map<String, String> {
        return mutableMapOf(
            ApiConstants.ARG_CLIENT_ID to clientId,
            ApiConstants.ARG_REDIRECT_URI to redirectUri,
            ApiConstants.ARG_CLIENT_SECRET to clientSecret,
            ApiConstants.ARG_CODE to code,
            ApiConstants.ARG_GRANT_TYPE to grantType
        ).toMap()
    }
}
