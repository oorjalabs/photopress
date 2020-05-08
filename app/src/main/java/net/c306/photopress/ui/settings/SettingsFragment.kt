package net.c306.photopress.ui.settings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import net.c306.photopress.ActivityViewModel
import net.c306.photopress.MainActivity
import net.c306.photopress.R
import net.c306.photopress.ui.custom.ConfirmationDialog
import net.c306.photopress.ui.custom.SearchableMultiSelectListPreference
import net.c306.photopress.ui.custom.SearchableMultiSelectListPreferenceDialogFragment
import net.c306.photopress.ui.newPost.NewPostViewModel
import net.c306.photopress.utils.AuthPrefs
import net.c306.photopress.utils.UserPrefs


class SettingsFragment : PreferenceFragmentCompat(), Preference.OnPreferenceClickListener {
    
    private val myTag = this::class.java.name
    
    private val activityViewModel by activityViewModels<ActivityViewModel>()
    private val confirmationViewModel: ConfirmationDialog.ConfirmationViewModel by activityViewModels()
    private val newPosViewModel: NewPostViewModel by activityViewModels()
    
    
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }
    
    
    override fun onDisplayPreferenceDialog(preference: Preference?) {
        val fm = parentFragment?.childFragmentManager
        
        when (preference) {
            /**
             * If this is our custom Preference, inflate and show dialog
             */
            is SearchableMultiSelectListPreference -> {
                SearchableMultiSelectListPreferenceDialogFragment.newInstance(preference.key).run {
                    fm?.let {
                        setTargetFragment(this@SettingsFragment, 0)
                        show(it, "android.support.v7.preference.PreferenceFragment.DIALOG")
                    }
                }
            }
            
            else                                   -> {
                super.onDisplayPreferenceDialog(preference)
            }
            
        }
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
        
        // Show logged in user's name
        findPreference<Preference>(KEY_PREF_LOGOUT)?.run {
            onPreferenceClickListener = this@SettingsFragment
        }
        
        
        activityViewModel.userDisplayName.observe(viewLifecycleOwner, Observer {
            if (it == null) return@Observer
            
            // Show logged in user's name
            findPreference<Preference>(KEY_LOGGED_IN_AS)?.run {
                title = getString(R.string.pref_title_logged_in, it)
            }
        })
        
        
        confirmationViewModel.result.observe(viewLifecycleOwner, Observer {
            if (it == null || it.callerTag != myTag) return@Observer
            
            confirmationViewModel.setResult(null)
            
            if (it.result) {
                // Logout
                activityViewModel.logout()
            }
        })
        
        
        newPosViewModel.blogTags.observe(viewLifecycleOwner, Observer { tags ->
    
            findPreference<SearchableMultiSelectListPreference>(UserPrefs.KEY_DEFAULT_TAGS)?.run {
                if (tags.isNullOrEmpty()) {
                    isEnabled = false
                } else {
                    isEnabled = true
                    val tagNames = tags.map { it.name }
                    entries = tagNames
                        .map { SearchableMultiSelectListPreference.Entry(it) }
                        .toTypedArray()
                }
            }
        })
        
    }
    
    
    /**
     * Handle non-persistent preference clicks
     */
    override fun onPreferenceClick(preference: Preference?): Boolean {
        when (preference?.key) {
            
            KEY_PREF_LOGOUT -> {
                //Show confirmation dialog, then logout
                findNavController().navigate(
                    SettingsFragmentDirections.actionOpenConfirmationDialog(
                        requestCode = RC_CONFIRM_LOGOUT,
                        dialogTitle = getString(R.string.pref_title_logout),
                        dialogMessage = getString(R.string.logout_confirm_text),
                        positiveButtonTitle = getString(R.string.pref_title_logout),
                        negativeButtonTitle = getString(R.string.string_cancel),
                        iconResourceId = R.drawable.ic_warning,
                        callerTag = myTag
                    )
                )
            }
            
            else            -> return false
            
        }
        return true // click was handled
    }
    
    
    companion object {
        const val KEY_OPEN_CREDITS = "key_open_credits"
        const val KEY_LOGGED_IN_AS = "key_pref_logged_in_as"
        const val KEY_PREF_LOGOUT = "key_pref_logout"
        
        const val RC_CONFIRM_LOGOUT = 2731
    }
}
