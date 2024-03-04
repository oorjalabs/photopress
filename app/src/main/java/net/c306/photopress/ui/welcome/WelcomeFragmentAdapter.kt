package net.c306.photopress.ui.welcome

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import net.c306.photopress.R
import net.c306.photopress.ui.custom.NoBottomNavFragment

class WelcomeFragmentAdapter(fragment: Fragment): FragmentStateAdapter(fragment) {

    private var mItemCount = Screens.entries.size

    internal fun setMaxScreen(value: Int) {
        mItemCount = value
    }

    override fun getItemCount() = mItemCount

    override fun createFragment(position: Int): Fragment =
        when (Screens.entries.getOrElse(position) { Screens.WELCOME }) {
            Screens.WELCOME -> NoBottomNavFragment(R.layout.fragment_welcome_item_init)
            Screens.LOGIN -> WelcomeItemFragmentLogin()
            Screens.SELECT_BLOG -> WelcomeItemFragmentSelectBlog()
        }

    enum class Screens {
        WELCOME,
        LOGIN,
        SELECT_BLOG
    }
}