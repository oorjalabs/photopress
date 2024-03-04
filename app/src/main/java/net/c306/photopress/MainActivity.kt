package net.c306.photopress

import android.app.ActivityManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import dagger.hilt.android.AndroidEntryPoint
import net.c306.customcomponents.updatenotes.UpdateNotesViewModel
import net.c306.customcomponents.utils.CommonUtils
import net.c306.photopress.databinding.ActivityMainBinding
import net.c306.photopress.ui.newPost.NewPostViewModel
import net.c306.photopress.ui.settings.SettingsFragment
import net.c306.photopress.ui.settings.SettingsFragmentDirections
import net.c306.photopress.utils.AppPrefs
import net.c306.photopress.utils.viewBinding
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    @Inject
    lateinit var appPrefs: AppPrefs

    private val navController: NavController
        get() = findNavController(R.id.nav_host_fragment)

    private val appViewModel by viewModels<AppViewModel>()
    private val newPostViewModel by viewModels<NewPostViewModel>()

    private val binding by viewBinding(ActivityMainBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set actual app theme. Theme in application/manifest is for splash
        setTheme(R.style.AppTheme)

        setTaskDescription(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityManager.TaskDescription.Builder()
                    .setLabel(getString(R.string.app_name))
                    .setIcon(R.mipmap.ic_launcher)
                    .setPrimaryColor(getColor(R.color.primaryColor))
                    .build()
            } else {
            ActivityManager.TaskDescription(
                getString(R.string.app_name), // Leave the default title.
                R.mipmap.ic_launcher,
                getColor(R.color.primaryColor) // Leave the default color
            )
            }
        )

        setContentView(binding.root)

        binding.navView.setupWithNavController(navController)

        appViewModel.isLoggedIn.observe(this) { }

        // Restart activity after logout
        appViewModel.doPostLogoutRestart.observe(this) {
            if (it != true) return@observe

            appViewModel.doPostLogoutRestart.value = false

            finish()
            startActivity(Intent.makeRestartActivityTask(componentName))
        }


        // If post tags are empty (app start or new post), set default tags as tags.
        // Don't use `newPost` instead because it causes image and input loss on configuration
        // change.
        newPostViewModel.defaultTags.observe(this, Observer {
            if (it.isNullOrBlank()) return@Observer

            if (newPostViewModel.postTags.value.isNullOrBlank()) {
                newPostViewModel.postTags.value = it
            }
        })

        // If post categories are empty (app start or new post), set default categories as categories.
        // Don't use `newPost` instead because it causes image and input loss on configuration
        // change.
        newPostViewModel.defaultCategories.observe(this, Observer {
            if (it.isNullOrEmpty()) return@Observer

            if (newPostViewModel.postCategories.isEmpty()) {
                newPostViewModel.postCategories = it
            }
        })

        // Set up Update notes viewModel for component view
        val updateNotesViewModel = ViewModelProvider(this)
            .get(UpdateNotesViewModel::class.java)
            .apply {
                setTitle(getString(R.string.title_update_notes))
                setShowOwnToolbar(true)
                setContentResourceId(net.c306.customcomponents.R.raw.updatenotes)
            }

        // Mark update notes seen when fragment is opened
        updateNotesViewModel.seen.observe(this, Observer {
            if (it != true) return@Observer
            /**
             * Mark update notes as seen.
             * Update app updated state and the app version in storage
             * only store version in storage when user has seen updates
             */
            with(appPrefs) {
                // Save new version code
                if (showUpdateNotes) {
                    saveAppVersion(BuildConfig.VERSION_CODE)
                    savePreviousNamedVersion(CommonUtils.getNamedVersion(BuildConfig.VERSION_NAME))
                    setShowUpdateNotes(false)
                }
            }
        })



        // If update notes available, highlight badge on settings tab
        appViewModel.showUpdateNotes.observe(this) { appUpdated ->
            if (appUpdated == true) {
                binding.navView.getOrCreateBadge(R.id.navigation_settings)
                    .isVisible = true
            } else {
                binding.navView.removeBadge(R.id.navigation_settings)
            }
        }

        // Handle share intent, if provided
        intent?.also { handleIntent(it) }
    }


    private fun handleIntent(intent: Intent) {

        if (intent.action == Intent.ACTION_SEND && intent.type?.startsWith("image/") == true) {
            (intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri)?.let {
                // Update UI to reflect image being shared
                newPostViewModel.setImageUris(listOf(it))
                this.intent = null
            }
        }
    }

    override fun onPreferenceStartFragment(
        caller: PreferenceFragmentCompat,
        pref: Preference
    ): Boolean {
        when (pref.key) {
            SettingsFragment.KEY_OPEN_CREDITS -> {
                navController.navigate(SettingsFragmentDirections.actionOpenCreditsFragment())
            }
        }
        return true
    }
}