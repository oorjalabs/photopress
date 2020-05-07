package net.c306.photopress.api

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * To save and fetch data from SharedPreferences
 */
class AuthPrefs (context: Context) {
    
    private var prefs: SharedPreferences = context.getSharedPreferences(AUTH_PREFS_NAME, Context.MODE_PRIVATE)
    
    companion object {
        
        const val ARG_USER_TOKEN = "user_token_ksf33n"
        const val ARG_USER_DETAILS = "arg_user_details_qkndi3"
        private const val ARG_BLOGS_LIST = "arg_blogs_list_ihd9323e"
        private const val ARG_TAGS_LIST = "arg_tags_list_sldfh329h"
        
        // This name is used in backup_descriptor to deny backups.
        // Change there too if you change here.
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
        return prefs.getString(ARG_USER_TOKEN, null) != null
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
    
    fun saveBlogsList(list: List<Blog>) {
        prefs.edit {
            putStringSet(ARG_BLOGS_LIST, list.map { it.toJson() }.toSet())
        }
    }
    
    fun getBlogsList(): List<Blog> {
        val savedSet = prefs.getStringSet(ARG_BLOGS_LIST, null)
            ?: return emptyList()
        
        return savedSet
            .map { Blog.fromJson(it) }
            .sortedBy { it.id }
    }
    
    fun saveTagsList(list: List<WPTag>?) {
        prefs.edit {
            
            if (list == null) {
                remove(ARG_TAGS_LIST)
            } else {
                putStringSet(
                    ARG_TAGS_LIST,
                    list
                        .distinctBy { it.id }
                        .map { it.toJson() }
                        .toSet()
                )
            }
        }
    }
    
    fun getTagsList(): List<WPTag>? {
        val savedSet = prefs.getStringSet(ARG_TAGS_LIST, null)
            ?: return null
        
        return savedSet
            .map { WPTag.fromJson(it) }
            .sortedBy { it.slug } // alphabetically, case independent (slugs are always lower case)
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