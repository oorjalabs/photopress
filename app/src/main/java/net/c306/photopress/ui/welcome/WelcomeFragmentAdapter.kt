package net.c306.photopress.ui.welcome

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class WelcomeFragmentAdapter(fragment: Fragment): FragmentStateAdapter(fragment) {

    private var mItemCount = 3

    internal fun setMaxScreen(value: Int) {
        mItemCount = value
    }

    override fun getItemCount() = mItemCount

    override fun createFragment(position: Int): Fragment {
        // Return a NEW fragment instance in createFragment(int)
        val fragment = WelcomeItemFragment()
        fragment.arguments = Bundle().apply {
            // Our object is just an integer :-P
            putInt(ARG_OBJECT, position + 1)
        }
        return fragment
    }

    companion object {
        internal const val ARG_OBJECT = "object"
    }
}