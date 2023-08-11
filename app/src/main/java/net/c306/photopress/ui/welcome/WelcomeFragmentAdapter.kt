package net.c306.photopress.ui.welcome

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import net.c306.photopress.R

class WelcomeFragmentAdapter(fragment: Fragment): FragmentStateAdapter(fragment) {

    private var mItemCount = 3

    internal fun setMaxScreen(value: Int) {
        mItemCount = value
    }

    override fun getItemCount() = mItemCount

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            Screens.SELECT_BLOG.screenNumber -> WelcomeItemFragmentSelectBlog()
            Screens.LOGIN.screenNumber     -> WelcomeItemFragmentLogin()
            else                             -> Fragment(R.layout.fragment_welcome_item_init)
        }
    }
    
    enum class Screens(val screenNumber: Int) {
        WELCOME (0),
        LOGIN (1),
        SELECT_BLOG (2)
    }
}