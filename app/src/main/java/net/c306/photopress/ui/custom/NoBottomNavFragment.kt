package net.c306.photopress.ui.custom

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import net.c306.photopress.MainActivity
import net.c306.photopress.R

open class NoBottomNavFragment: Fragment {
    
    constructor(): super()
    constructor(@LayoutRes layout: Int): super(layout)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? MainActivity)?.apply {
            findViewById<BottomNavigationView>(R.id.nav_view)?.visibility = View.GONE
        }
    }

}