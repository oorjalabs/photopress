package net.c306.photopress.ui.welcome

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class WelcomeFragmentAdapter(fragment: Fragment): FragmentStateAdapter(fragment) {

    private var mItemCount = 3

    internal fun setMaxScreen(value: Int) {
        mItemCount = value
    }

    override fun getItemCount() = mItemCount

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            2 -> WelcomeItemFragmentThree()
            1 -> WelcomeItemFragmentTwo()
            else -> WelcomeItemFragmentOne()
        }
    }

}