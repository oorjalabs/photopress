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
import net.c306.customcomponents.updatenotes.UpdateNotesViewModel
import net.c306.customcomponents.utils.CommonUtils
import net.c306.photopress.databinding.ActivityMainBinding
import net.c306.photopress.ui.newPost.NewPostViewModel
import net.c306.photopress.ui.settings.SettingsFragment
import net.c306.photopress.ui.settings.SettingsFragmentDirections
import net.c306.photopress.utils.AppPrefs

class MainActivity : AppCompatActivity(), PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {
    
    private val navController: NavController
        get() = findNavController(R.id.nav_host_fragment)
    
    private val appViewModel by viewModels<AppViewModel>()
    private val newPostViewModel by viewModels<NewPostViewModel>()
    
    private lateinit var binding: ActivityMainBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Set actual app theme. Theme in application/manifest is for splash
        setTheme(R.style.AppTheme)
        
        // On Android P+, set app icon in app switcher view
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            setTaskDescription(
                ActivityManager.TaskDescription(
                    getString(R.string.app_name), // Leave the default title.
                    R.mipmap.ic_launcher,
                    getColor(R.color.primaryColor) // Leave the default color
                )
            )
        }
    
        binding = ActivityMainBinding.inflate(layoutInflater)
        
        setContentView(binding.root)
        
        binding.navView.setupWithNavController(navController)
        
        appViewModel.isLoggedIn.observe(this, {  })
        appViewModel.selectedBlogId.observe(this, {  })
        appViewModel.blogSelected.observe(this, {  })
        
        // Restart activity after logout
        appViewModel.doPostLogoutRestart.observe(this, Observer {
            if (it != true) return@Observer
            
            appViewModel.doPostLogoutRestart.value = false
            
            finish()
            startActivity(Intent.makeRestartActivityTask(componentName))
        })
        
        
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
            
            if (newPostViewModel.postCategories.isNullOrEmpty()) {
                newPostViewModel.postCategories = it
            }
        })
        
        // Set up Update notes viewModel for component view
        val updateNotesViewModel = ViewModelProvider(this)
            .get(UpdateNotesViewModel::class.java)
            .apply {
                setTitle(getString(R.string.title_update_notes))
                setShowOwnToolbar(true)
                setContentResourceId(R.raw.updatenotes)
            }
        
        // Mark update notes seen when fragment is opened
        updateNotesViewModel.seen.observe(this, Observer {
            if (it != true) return@Observer
            /**
             * Mark update notes as seen.
             * Update app updated state and the app version in storage
             * only store version in storage when user has seen updates
             */
            AppPrefs.getInstance(applicationContext).run {
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