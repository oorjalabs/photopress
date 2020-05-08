package net.c306.photopress.utils

import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * To save and fetch data from SharedPreferences
 */
abstract class BasePrefs {
    
    protected abstract var prefs: SharedPreferences
    
    fun observe(observer: SharedPreferences.OnSharedPreferenceChangeListener) {
        prefs.registerOnSharedPreferenceChangeListener(observer)
    }
    
    /**
     * Clear all auth and user related data. Used on logout.
     */
    fun clear() {
        prefs.edit(commit = true) {
            clear()
        }
    }
}