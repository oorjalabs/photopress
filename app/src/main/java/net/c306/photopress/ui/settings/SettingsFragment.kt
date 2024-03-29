package net.c306.photopress.ui.settings

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import net.c306.customcomponents.confirmation.ConfirmationDialog
import net.c306.customcomponents.preference.CustomPreferenceFragment
import net.c306.customcomponents.preference.SearchableListPreference
import net.c306.photopress.AppViewModel
import net.c306.photopress.MainActivity
import net.c306.photopress.R
import net.c306.photopress.ui.newPost.NewPostViewModel
import net.c306.photopress.utils.AuthPrefs
import net.c306.photopress.utils.Settings
import net.c306.photopress.utils.setCustomDefaultValue
import javax.inject.Inject


@AndroidEntryPoint
internal class SettingsFragment : CustomPreferenceFragment(), Preference.OnPreferenceClickListener {

    @Inject
    lateinit var authPrefs: AuthPrefs

    private val myTag = this::class.java.name

    private val appViewModel by activityViewModels<AppViewModel>()
    private val confirmationViewModel: ConfirmationDialog.ConfirmationViewModel by activityViewModels()
    private val newPostViewModel: NewPostViewModel by activityViewModels()


    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Do this to ensure that bottom nav is visible when we return from a fragment which hides it
        (activity as? MainActivity)
            ?.findViewById<BottomNavigationView>(R.id.nav_view)?.isVisible = true

        // Set user's blog list
        findPreference<SearchableListPreference>(Settings.KEY_SELECTED_BLOG_ID)?.run {
            val blogs = authPrefs.getBlogsList()

            entries = blogs.map {
                SearchableListPreference.Entry(
                    entry = it.name,
                    value = it.id.toString(),
                    enabled = true
                )
            }.toTypedArray()

            setOnPreferenceChangeListener { _, _ ->
                // Clear saved tags list when blog changes
                authPrefs.saveTagsList(null)
                authPrefs.saveCategoriesList(null)
                true
            }
        }

        // Setup blog format preference
        findPreference<SearchableListPreference>(Settings.KEY_PUBLISH_FORMAT)?.run {
            val entriesList = resources.getStringArray(R.array.pref_entries_post_format)
            val valuesList = resources.getStringArray(R.array.pref_values_post_format)

            if (entriesList.size != valuesList.size) throw Exception("Entries and values are not the same size.")

            entries = entriesList.mapIndexed { index, s ->
                SearchableListPreference.Entry(
                    entry = s,
                    value = valuesList[index],
                    enabled = true
                )
            }.toTypedArray()

            setCustomDefaultValue(Settings.PUBLISH_FORMAT_BLOCK)
        }

        findPreference<Preference>(KEY_PREF_LOGOUT)?.onPreferenceClickListener = this

        // Set up update notes preference
        appViewModel.showUpdateNotes.observe(viewLifecycleOwner) {
            findPreference<Preference>(KEY_UPDATE_NOTES)?.apply {
                onPreferenceClickListener = this@SettingsFragment
                isVisible = it == true
            }
            findPreference<Preference>(KEY_UPDATE_NOTES_BOTTOM)?.apply {
                onPreferenceClickListener = this@SettingsFragment
                isVisible = it != true
            }
        }


        // Show logged in user's name
        appViewModel.userDisplayName.observe(viewLifecycleOwner, Observer {
            if (it == null) return@Observer

            // Show logged in user's name
            findPreference<Preference>(KEY_LOGGED_IN_AS)?.run {
                title = getString(R.string.pref_title_logged_in, it)
            }
        })


        confirmationViewModel.result.observe(viewLifecycleOwner, Observer {
            if (it == null || it.callerTag != myTag) return@Observer

            confirmationViewModel.reset()

            // Logout
            if (it.result) {
                appViewModel.logout()
            }
        })

        // Set up default tags preference
        newPostViewModel.blogTags.observe(viewLifecycleOwner) { tags ->
            findPreference<SearchableListPreference>(Settings.KEY_DEFAULT_TAGS)?.run {
                if (tags.isNullOrEmpty()) {
                    isEnabled = false
                    entries = emptyArray()
                } else {
                    isEnabled = true
                    entries = tags
                        .map { SearchableListPreference.Entry(it.name) }
                        .toTypedArray()
                }
            }
        }

        // Set up default categories preference
        newPostViewModel.blogCategories.observe(viewLifecycleOwner) { categories ->
            findPreference<SearchableListPreference>(Settings.KEY_DEFAULT_CATEGORIES)?.run {
                if (categories.isNullOrEmpty()) {
                    isEnabled = false
                    entries = emptyArray()
                } else {
                    isEnabled = true
                    entries = categories
                        .map { SearchableListPreference.Entry(it.name) }
                        .toTypedArray()
                }
            }
        }

    }


    /**
     * Handle non-persistent preference clicks
     */
    override fun onPreferenceClick(preference: Preference): Boolean {
        when (preference.key) {

            KEY_PREF_LOGOUT -> {
                //Show confirmation dialog, then logout
                findNavController().navigate(
                    R.id.confirmationDialog,
                    bundleOf(ConfirmationDialog.KEY_CONFIRMATION_DETAILS to ConfirmationDialog.Details(
                        requestCode = RC_CONFIRM_LOGOUT,
                        dialogTitle = getString(R.string.pref_title_logout),
                        dialogMessage = getString(R.string.logout_confirm_text),
                        positiveButtonTitle = getString(R.string.pref_title_logout),
                        iconResourceId = R.drawable.ic_logout,
                        callerTag = myTag
                    ))
                )
            }

            KEY_UPDATE_NOTES, KEY_UPDATE_NOTES_BOTTOM -> {
                findNavController().navigate(SettingsFragmentDirections.actionShowUpdateNotes())
            }

            else            -> return false

        }
        return true // click was handled
    }


    companion object {
        const val KEY_OPEN_CREDITS = "key_open_credits"
        const val KEY_LOGGED_IN_AS = "key_pref_logged_in_as"
        const val KEY_PREF_LOGOUT = "key_pref_logout"
        const val KEY_UPDATE_NOTES = "key_whats_new"
        const val KEY_UPDATE_NOTES_BOTTOM = "key_update_notes_bottom"

        const val RC_CONFIRM_LOGOUT = 2731
    }
}