package net.c306.photopress.api

import androidx.annotation.Keep
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

@Keep
data class UserDetails(
    @SerializedName(ApiConstants.ARG_USER_DISPLAY_NAME)
    val displayName: String? = null,

    val username: String? = null,

    val email: String? = null,

    @SerializedName(ApiConstants.ARG_USER_AVATAR_URL)
    val avatarUrl: String? = null,

    @SerializedName(ApiConstants.ARG_USER_PROFILE_URL)
    val profileUrl: String? = null
) {

    fun toJson(): String {
        return Gson().toJson(this)
    }

    companion object {
        fun fromJson(jsonString: String): UserDetails {
            return Gson().fromJson(jsonString, UserDetails::class.java)
        }

        const val FIELD_STRING = "display_name,username,email,avatar_URL,profile_URL"
    }
}