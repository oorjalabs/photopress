package net.c306.photopress.ui.settings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import net.c306.photopress.ActivityViewModel
import net.c306.photopress.MainActivity
import net.c306.photopress.R
import net.c306.photopress.utils.AuthPrefs
import net.c306.photopress.utils.UserPrefs

class SettingsFragment : PreferenceFragmentCompat() {
    
    private val activityViewModel by activityViewModels<ActivityViewModel>()
    
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        (activity as? MainActivity)?.apply {
            findViewById<BottomNavigationView>(R.id.nav_view)?.visibility = View.VISIBLE
        }
        
        // Set user's blog list
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
        
        activityViewModel.isLoggedIn.observe(viewLifecycleOwner, Observer {
            // TODO("Change login and logout messages based on value")
        })
        
        activityViewModel.userDisplayName.observe(viewLifecycleOwner, Observer {
            if (it == null) return@Observer
            
            // Show logged in user's name
            findPreference<Preference>(KEY_LOGGED_IN_AS)?.run {
                title = getString(R.string.pref_title_logged_in, it)
            }
        })
        
    }
    
    companion object {
        const val KEY_OPEN_CREDITS = "key_open_credits"
        const val KEY_LOGGED_IN_AS = "key_pref_logged_in_as"
    }
    
}
