package net.c306.photopress

import android.app.ActivityManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val navController: NavController
        get() = findNavController(R.id.nav_host_fragment)
    
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

        setContentView(R.layout.activity_main)

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(setOf(
                R.id.navigation_post_list, R.id.navigation_post_new, R.id.navigation_settings))
//        setupActionBarWithNavController(navController, appBarConfiguration)

        nav_view?.setupWithNavController(navController)
    }
}
