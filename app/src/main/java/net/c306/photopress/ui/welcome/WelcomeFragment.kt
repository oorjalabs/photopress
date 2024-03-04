package net.c306.photopress.ui.welcome

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import dagger.hilt.android.AndroidEntryPoint
import net.c306.customcomponents.utils.getFloatFromXml
import net.c306.photopress.AppViewModel
import net.c306.photopress.R
import net.c306.photopress.core.extensions.viewBinding
import net.c306.photopress.databinding.FragmentWelcomeBinding
import net.c306.photopress.ui.custom.NoBottomNavFragment
import net.c306.photopress.ui.welcome.WelcomeFragmentAdapter.Screens

/**
 * Holder fragment for the welcome fragment views
 */
@AndroidEntryPoint
class WelcomeFragment : NoBottomNavFragment(R.layout.fragment_welcome) {

    // When requested, this adapter returns a DemoObjectFragment,
    // representing an object in the collection.
    private val mPagerAdapter: WelcomeFragmentAdapter by lazy {
        WelcomeFragmentAdapter(this)
    }

    private val args by navArgs<WelcomeFragmentArgs>()

    private val appViewModel by activityViewModels<AppViewModel>()
    private val viewModel by activityViewModels<WelcomeViewModel>()

    private val binding by viewBinding(FragmentWelcomeBinding::bind)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // If already set up, go straight to main app
        val isLoggedIn = appViewModel.isLoggedIn.value == true
        val blogSelected = viewModel.isBlogSelected.value
        if (isLoggedIn && blogSelected) {
            findNavController().navigate(WelcomeFragmentDirections.actionGoToApp())
            return null
        }

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.pager.apply {
            adapter = mPagerAdapter

            // Open to screen number specified in args, if valid
            if (args.startScreenNumber in 0..mPagerAdapter.itemCount) {
                setCurrentItem(args.startScreenNumber, false)
            }

            registerOnPageChangeCallback(onPageSelectedListener)

            setPageIndicators(args.startScreenNumber)
        }


        // If not authenticated, disable screen 3
        appViewModel.isLoggedIn.observe(viewLifecycleOwner) {
            val loggedIn = it == true

            mPagerAdapter.setMaxScreen(
                1 + if (loggedIn) {
                    Screens.SELECT_BLOG.ordinal
                } else {
                    Screens.LOGIN.ordinal
                }
            )
        }

        viewModel.goToScreen.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.pager.setCurrentItem(it, true)
                viewModel.setGoToScreen(null)
            }
        }

        binding.buttonCloseWelcome.setOnClickListener {
            goToApp()
        }

        appViewModel.isLoggedIn.observe(viewLifecycleOwner) {
            binding.progressIndicatorPage3.alpha = if (it) {
                defaultIconAlpha
            } else {
                disabledIconAlpha
            }
        }
    }

    override fun onDestroyView() {
//        binding.pager.unregisterOnPageChangeCallback(onPageSelectedListener)
        super.onDestroyView()
    }

    private fun setPageIndicators(pageIndex: Int) {
        binding.progressIndicatorPage1.setImageDrawable(getCircleIndicator(pageIndex == 0))
        binding.progressIndicatorPage2.setImageDrawable(getCircleIndicator(pageIndex == 1))
        binding.progressIndicatorPage3.setImageDrawable(getCircleIndicator(pageIndex == 2))
    }

    private fun getCircleIndicator(isSelected: Boolean): Drawable? {
        val context = requireContext()
        val filledCircle = ContextCompat.getDrawable(context, R.drawable.ic_circle_filled)
        val emptyCircle = ContextCompat.getDrawable(context, R.drawable.ic_circle_empty)
        return if (isSelected) filledCircle else emptyCircle
    }

    private fun goToApp() {
        findNavController().navigate(WelcomeFragmentDirections.actionGoToApp())
    }

    private val disabledIconAlpha by lazy { requireContext().getFloatFromXml(net.c306.customcomponents.R.dimen.icon_alpha_disabled) }
    private val defaultIconAlpha by lazy { requireContext().getFloatFromXml(net.c306.customcomponents.R.dimen.icon_alpha_default) }

    private val onPageSelectedListener = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            setPageIndicators(position)
        }
    }
}