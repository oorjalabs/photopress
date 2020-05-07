package net.c306.photopress.ui.custom

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import net.c306.photopress.MainActivity
import net.c306.photopress.R

open class BottomNavFragment: Fragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? MainActivity)?.apply {
            findViewById<BottomNavigationView>(R.id.nav_view)?.visibility = View.VISIBLE
        }
    }

}