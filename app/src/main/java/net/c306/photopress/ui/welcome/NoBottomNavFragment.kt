package net.c306.photopress.ui.welcome

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import net.c306.photopress.MainActivity
import net.c306.photopress.R

open class NoBottomNavFragment: Fragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? MainActivity)?.apply {
            findViewById<BottomNavigationView>(R.id.nav_view)?.visibility = View.GONE
        }
    }

//    override fun onDestroyView() {
//        (activity as? MainActivity)?.apply {
//            findViewById<BottomNavigationView>(R.id.nav_view)?.visibility = View.VISIBLE
//        }
//
//        super.onDestroyView()
//    }

}