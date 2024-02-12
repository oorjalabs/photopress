package net.c306.photopress.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

/**
 * To save and fetch data from SharedPreferences
 */
@Singleton
class AppPrefs @Inject constructor(
    @ApplicationContext context: Context
) : BasePrefs() {

    override var prefs: SharedPreferences =
        context.getSharedPreferences(APP_PREFS_NAME, Context.MODE_PRIVATE)

    internal fun saveAppVersion(versionCode: Int) {
        prefs.edit { putInt(KEY_APP_VERSION, versionCode) }
    }

    internal val appVersion: Int?
        get() {
            val savedAppVersion = prefs.getInt(KEY_APP_VERSION, -1)
            return if (savedAppVersion > -1) savedAppVersion else null
        }

    internal fun savePreviousNamedVersion(namedVersion: BigDecimal) {
        prefs.edit { putFloat(KEY_NAMED_APP_VERSION, namedVersion.toFloat()) }
    }

    internal val previousNamedVersion: BigDecimal
        get() = prefs.getFloat(KEY_NAMED_APP_VERSION, -1F).toBigDecimal()

    internal fun setShowUpdateNotes(show: Boolean) {
        prefs.edit { putBoolean(KEY_SHOW_UPDATE_NOTES, show) }
    }

    internal val showUpdateNotes: Boolean
        get() = prefs.getBoolean(KEY_SHOW_UPDATE_NOTES, false)

    internal val firstUseTimestamp: Long
        get() = prefs.getLong(KEY_FIRST_USE_TIMESTAMP, -1L)

    internal fun setFirstUseTimestamp(ts: Long) {
        prefs.edit { putLong(KEY_FIRST_USE_TIMESTAMP, ts) }
    }

    companion object {

        const val ARG_USER_TOKEN = "user_token_ksf33n"
        const val ARG_USER_DETAILS = "arg_user_details_qkndi3"
        private const val ARG_BLOGS_LIST = "arg_blogs_list_ihd9323e"
        private const val ARG_TAGS_LIST = "arg_tags_list_sldfh329h"

        private const val KEY_APP_VERSION = "key_app_version"
        private const val KEY_NAMED_APP_VERSION = "key_named_app_version"
        internal const val KEY_SHOW_UPDATE_NOTES = "key_app_updated_ohf8whf38bi"
        private const val KEY_FIRST_USE_TIMESTAMP = "key_first_use_timestamp_lsbcq38ubcj"


        // This name is used in backup_descriptor to deny backups.
        // Change there too if you change here.
        private const val APP_PREFS_NAME = "SL6DpzcSt1jL2aIZ"
    }
}