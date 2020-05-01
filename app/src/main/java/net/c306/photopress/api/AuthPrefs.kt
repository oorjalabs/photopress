package net.c306.photopress.api

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import timber.log.Timber

/**
 * To save and fetch data from SharedPreferences
 */
class AuthPrefs (context: Context) {

    private var prefs: SharedPreferences = context.getSharedPreferences(AUTH_PREFS_NAME, Context.MODE_PRIVATE)

    companion object {

        const val ARG_USER_TOKEN = "user_token_ksf33n"
        const val ARG_USER_DETAILS = "arg_user_details_qkndi3"

        private const val AUTH_PREFS_NAME = "gdeu82gd823eg339h238ghf"
    }

    fun observe(observer: SharedPreferences.OnSharedPreferenceChangeListener) {
        prefs.registerOnSharedPreferenceChangeListener(observer)
    }

    /**
     * Function to save auth token
     */
    fun saveAuthToken(token: String) {
        prefs.edit {
            putString(ARG_USER_TOKEN, token)
        }
    }

    /**
     * Function to fetch auth token
     */
    fun haveAuthToken(): Boolean {
        val haveIt = prefs.getString(ARG_USER_TOKEN, null) != null
        Timber.d("have Auth token: $haveIt")
        return haveIt
    }

    /**
     * Function to fetch auth token
     */
    fun fetchAuthToken(): String? {
        return prefs.getString(ARG_USER_TOKEN, null)
    }

    fun getUserDetails(): UserDetails? {
        val savedString = prefs.getString(ARG_USER_DETAILS, null) ?: return null

        return UserDetails.fromJson(savedString)
    }

    fun saveUserDetails(details: UserDetails) {
        prefs.edit {
            putString(ARG_USER_DETAILS, details.toJson())
        }
    }

    fun clear() {
        prefs.edit(commit = true) {
            clear()
        }
    }
}