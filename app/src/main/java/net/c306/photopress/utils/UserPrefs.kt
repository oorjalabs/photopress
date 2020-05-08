package net.c306.photopress.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager

/**
 * To save and fetch data from SharedPreferences
 */
class UserPrefs (context: Context): BasePrefs() {
    
    override var prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    
    companion object {
        const val KEY_SELECTED_BLOG_ID = "key_selected_blog_id"
        const val KEY_PUBLISH_FORMAT = "key_publish_format"
        const val KEY_ADD_FEATURED_IMAGE = "key_add_featured_image"
        const val KEY_DEFAULT_TAGS = "key_default_tags"
        const val KEY_DEFAULT_CATEGORIES = "key_default_categories"
        
        const val DEFAULT_USE_BLOCK_EDITOR = true
        const val DEFAULT_ADD_FEATURED_IMAGE = true
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
    
    
    fun getUseBlockEditor(): Boolean {
        val publishFormat = prefs.getString(KEY_PUBLISH_FORMAT, null)
                            ?: return DEFAULT_USE_BLOCK_EDITOR
        return publishFormat == "block"
    }
    
    
    fun getAddFeaturedImage(): Boolean {
        return prefs.getBoolean(KEY_ADD_FEATURED_IMAGE, DEFAULT_ADD_FEATURED_IMAGE)
    }
}