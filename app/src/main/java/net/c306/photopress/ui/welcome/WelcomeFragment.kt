package net.c306.photopress.ui.welcome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import kotlinx.android.synthetic.main.fragment_welcome.*
import net.c306.photopress.ActivityViewModel
import net.c306.photopress.R

/**
 * Holder fragment for the welcome fragment views
 */
class WelcomeFragment : NoBottomNavFragment() {

    // When requested, this adapter returns a DemoObjectFragment,
    // representing an object in the collection.
    private val mPagerAdapter: WelcomeFragmentAdapter by lazy {
        WelcomeFragmentAdapter(this)
    }


    private val args by navArgs<WelcomeFragmentArgs>()

    private val activityViewModel by activityViewModels<ActivityViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val isLoggedIn = activityViewModel.isLoggedIn.value == true
        val blogSelected = activityViewModel.blogSelected.value == true
        if (isLoggedIn && blogSelected) {
            findNavController().navigate(WelcomeFragmentDirections.actionGoToApp())
            return null
        }
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_welcome, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pager?.apply {
            adapter = mPagerAdapter

            // Open to screen number specified in args, if valid
            if (args.startScreenNumber in 0..mPagerAdapter.itemCount) {
                setCurrentItem(args.startScreenNumber, false)
            }
        }

        // Close welcome screen on close button press (Not used)
        button_close_welcome?.apply {
            // TODO("Set this to true if coming after first launch and login")
            visibility = View.GONE

            setOnClickListener {
                findNavController().navigate(WelcomeFragmentDirections.actionGoToApp())
            }
        }

        // If not authenticated, disable screen 3
        activityViewModel.isLoggedIn.observe(viewLifecycleOwner, Observer {
            mPagerAdapter.setMaxScreen(
                if (it == true) 3 else 2
            )
        })

    }

}
