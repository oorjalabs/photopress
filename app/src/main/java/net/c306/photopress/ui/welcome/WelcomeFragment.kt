package net.c306.photopress.ui.welcome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_welcome.*
import net.c306.photopress.R

/**
 * Holder fragment for the welcome fragment views
 */
class WelcomeFragment : NoBottomNavFragment() {

    // When requested, this adapter returns a DemoObjectFragment,
    // representing an object in the collection.
    private lateinit var mPagerAdapter: WelcomeFragmentAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_welcome, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pager?.apply {
            mPagerAdapter = WelcomeFragmentAdapter(this@WelcomeFragment)
            adapter = mPagerAdapter
            mPagerAdapter.setMaxScreen(3)
        }

        button_close_welcome?.apply {
            // TODO("Set this to true if coming after first launch and login")
            visibility = View.GONE

            setOnClickListener {
                findNavController().navigate(WelcomeFragmentDirections.actionWelcomeToPost())
            }
        }
    }

}
