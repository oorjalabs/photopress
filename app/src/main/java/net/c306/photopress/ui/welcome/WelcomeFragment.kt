package net.c306.photopress.ui.welcome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_welcome.*
import net.c306.photopress.ActivityViewModel
import net.c306.photopress.R
import timber.log.Timber

/**
 * Holder fragment for the welcome fragment views
 */
class WelcomeFragment : NoBottomNavFragment() {

    // When requested, this adapter returns a DemoObjectFragment,
    // representing an object in the collection.
    private val mPagerAdapter: WelcomeFragmentAdapter by lazy {
        WelcomeFragmentAdapter(this)
    }

    private val activityViewModel by activityViewModels<ActivityViewModel>()
    private val welcomeViewModel by activityViewModels<WelcomeViewModel>()

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
            adapter = mPagerAdapter
            // TODO("Set current item from arguments")
//            currentItem = 1
        }

        button_close_welcome?.apply {
            // TODO("Set this to true if coming after first launch and login")
            visibility = View.GONE

            setOnClickListener {
                findNavController().navigate(WelcomeFragmentDirections.actionWelcomeToPost())
            }
        }

        // If not authenticated, disable screen 3
        activityViewModel.isLoggedIn.observe(viewLifecycleOwner, Observer {
            Timber.d("isLoggedIn: $it")
            mPagerAdapter.setMaxScreen(
                if (it == true) 3 else 2
            )
        })


        welcomeViewModel.openLoginScreen.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                welcomeViewModel.setOpenLoginScreen(false)
                findNavController().navigate(
                    WelcomeFragmentDirections.actionOpenLoginFragment()
                )
            }
        })

    }

}
