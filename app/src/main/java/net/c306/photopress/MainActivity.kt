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
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import kotlinx.android.synthetic.main.activity_main.*
import net.c306.photopress.ui.newPost.NewPostViewModel
import net.c306.photopress.ui.settings.SettingsFragment
import net.c306.photopress.ui.settings.SettingsFragmentDirections

class MainActivity : AppCompatActivity(), PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    private val navController: NavController
        get() = findNavController(R.id.nav_host_fragment)

    private val activityViewModel by viewModels<ActivityViewModel>()
    private val newPostViewModel by viewModels<NewPostViewModel>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        if (BuildConfig.DEBUG) {
//        }

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


        setContentView(R.layout.activity_main)

        nav_view?.setupWithNavController(navController)

        activityViewModel.isLoggedIn.observe(this, Observer {  })
        activityViewModel.selectedBlogId.observe(this, Observer {  })
        activityViewModel.blogSelected.observe(this, Observer {  })
        
        // Restart activity after logout
        activityViewModel.doPostLogoutRestart.observe(this, Observer {
            if (it != true) return@Observer
            
            activityViewModel.doPostLogoutRestart.value = false
            
            finish()
            startActivity(Intent.makeRestartActivityTask(componentName))
        })
        
        // Handle share intent, if provided
        intent?.also { intent ->
            if (intent.action == Intent.ACTION_SEND && intent.type?.startsWith("image/") == true) {
                (intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri)?.let {
                    // Update UI to reflect image being shared
                    newPostViewModel.setImageUri(it)
                    this.intent = null
                }
            }
        }
    }
    
    
    override fun onPreferenceStartFragment(
        caller: PreferenceFragmentCompat?,
        pref: Preference?
    ): Boolean {
        when (pref?.key) {
            SettingsFragment.KEY_OPEN_CREDITS -> {
                navController.navigate(SettingsFragmentDirections.actionOpenCreditsFragment())
            }
        }
        return true
    }
}
