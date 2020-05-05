package net.c306.photopress.ui.settings

import android.os.Bundle
import android.view.View
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import net.c306.photopress.R
import net.c306.photopress.UserPrefs
import net.c306.photopress.api.AuthPrefs

class SettingsFragment : PreferenceFragmentCompat() {
    
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Set currency list values
        findPreference<ListPreference>(UserPrefs.KEY_SELECTED_BLOG_ID)?.run {
            val authPrefs = AuthPrefs(context)
            val blogs = authPrefs.getBlogsList()
            entries = blogs.map { blog -> blog.name }.toTypedArray()
            entryValues = blogs.map { it.id.toString() }.toTypedArray()
            
            setOnPreferenceChangeListener { _, _ ->
                // Clear saved tags list when blog changes
                authPrefs.saveTagsList(null)
                true
            }
        }
        
    }
    
}
