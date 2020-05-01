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

        const val USER_TOKEN = "user_token"
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
            putString(USER_TOKEN, token)
        }
    }

    fun removeAuthToken() {
        prefs.edit (commit = true) {
            remove(USER_TOKEN)
        }
    }

    /**
     * Function to fetch auth token
     */
    fun haveAuthToken(): Boolean {
        val haveIt = prefs.getString(USER_TOKEN, null) != null
        Timber.d("have Auth token: $haveIt")
        return haveIt
    }

    /**
     * Function to fetch auth token
     */
    fun fetchAuthToken(): String? {
        return prefs.getString(USER_TOKEN, null)
    }
}