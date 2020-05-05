package net.c306.photopress

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager

/**
 * To save and fetch data from SharedPreferences
 */
class UserPrefs (context: Context) {
    
    private var prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    
    companion object {
        const val KEY_SELECTED_BLOG_ID = "key_selected_blog_id"
    }
    
    fun observe(observer: SharedPreferences.OnSharedPreferenceChangeListener) {
        prefs.registerOnSharedPreferenceChangeListener(observer)
    }
    
    fun setSelectedBlogId(value: Int) {
        prefs.edit {
            if (value < 0) {
                remove(KEY_SELECTED_BLOG_ID)
            } else {
                putString(KEY_SELECTED_BLOG_ID, value.toString())
            }
        }
    }
    
    fun getSelectedBlogId(): Int {
        val value = prefs.getString(KEY_SELECTED_BLOG_ID, null) ?: return -1
        return value.toInt()
    }
    
    /**
     * Clear all auth and user related data. Used on logout.
     */
    @Suppress("unused")
    fun clear() {
        prefs.edit(commit = true) {
            clear()
        }
    }
}