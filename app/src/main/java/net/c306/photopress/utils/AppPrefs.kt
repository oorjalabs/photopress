package net.c306.photopress.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import java.math.BigDecimal

/**
 * To save and fetch data from SharedPreferences
 */
class AppPrefs (context: Context) : BasePrefs() {
    
    override var prefs: SharedPreferences = context.getSharedPreferences(APP_PREFS_NAME, Context.MODE_PRIVATE)
    
    companion object {
        
        const val ARG_USER_TOKEN = "user_token_ksf33n"
        const val ARG_USER_DETAILS = "arg_user_details_qkndi3"
        private const val ARG_BLOGS_LIST = "arg_blogs_list_ihd9323e"
        private const val ARG_TAGS_LIST = "arg_tags_list_sldfh329h"
        
        private const val KEY_APP_VERSION = "key_app_version"
        private const val KEY_NAMED_APP_VERSION = "key_named_app_version"
        private const val KEY_SHOW_UPDATE_NOTES = "key_app_updated_ohf8whf38bi"
        
        
        // This name is used in backup_descriptor to deny backups.
        // Change there too if you change here.
        private const val APP_PREFS_NAME = "SL6DpzcSt1jL2aIZ"
    }
    
    internal fun saveAppVersion(versionCode: Int) {
        prefs.edit {
            putInt(KEY_APP_VERSION, versionCode)
        }
    }
    
    internal fun getAppVersion(): Int? {
        val savedAppVersion = prefs.getInt(KEY_APP_VERSION, -1)
        return if (savedAppVersion > -1) savedAppVersion else null
    }
    
    internal fun savePreviousNamedVersion(namedVersion: BigDecimal) {
        return prefs.edit {
            putFloat(KEY_NAMED_APP_VERSION, namedVersion.toFloat())
        }
    }
    
    internal fun getPreviousNamedVersion(): BigDecimal {
        return prefs.getFloat(KEY_NAMED_APP_VERSION, -1F)
            .toBigDecimal()
    }
    
    internal fun setShowUpdateNotes(show: Boolean) {
        prefs.edit {
            putBoolean(KEY_SHOW_UPDATE_NOTES, show)
        }
    }
    
    internal fun getShowUpdateNotes(): Boolean = prefs
        .getBoolean(KEY_SHOW_UPDATE_NOTES, false)
    
}