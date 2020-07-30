package net.c306.photopress.ui.welcome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import net.c306.customcomponents.utils.getFloatFromXml
import net.c306.photopress.ActivityViewModel
import net.c306.photopress.R
import net.c306.photopress.databinding.FragmentWelcomeBinding
import net.c306.photopress.ui.custom.NoBottomNavFragment
import net.c306.photopress.ui.welcome.WelcomeFragmentAdapter.Screens

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
    private val welcomeViewModel by activityViewModels<WelcomeViewModel>()
    
    private lateinit var binding: FragmentWelcomeBinding
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        
        // If already set up, go straight to main app
        val isLoggedIn = activityViewModel.isLoggedIn.value == true
        val blogSelected = activityViewModel.blogSelected.value == true
        if (isLoggedIn && blogSelected) {
            findNavController().navigate(WelcomeFragmentDirections.actionGoToApp())
            return null
        }
        
        // Inflate the layout for this fragment
        binding = FragmentWelcomeBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
            activityViewModel = this@WelcomeFragment.activityViewModel
            handler = Handler()
        }
        
        return binding.root
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
        activityViewModel.isLoggedIn.observe(viewLifecycleOwner, Observer {
            val loggedIn = it == true
            
            mPagerAdapter.setMaxScreen(
                1 + if (loggedIn) Screens.SELECT_BLOG.screenNumber else Screens.LOGIN.screenNumber
            )
        })
        
        
        welcomeViewModel.goToScreen.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                binding.pager.setCurrentItem(it, true)
                welcomeViewModel.setGoToScreen(null)
            }
        })
        
    }
    
    
    override fun onDestroyView() {
        if (::binding.isInitialized) {
            binding.pager.unregisterOnPageChangeCallback(onPageSelectedListener)
        }
        super.onDestroyView()
    }
    
    
    private fun setPageIndicators(pageIndex: Int) {
        val context = requireContext()
        val filledCircle = context.getDrawable(R.drawable.ic_circle_filled)
        val emptyCircle = context.getDrawable(R.drawable.ic_circle_empty)
        
        binding.progressIndicatorPage1.setImageDrawable(if (pageIndex == 0) filledCircle else emptyCircle)
        binding.progressIndicatorPage2.setImageDrawable(if (pageIndex == 1) filledCircle else emptyCircle)
        binding.progressIndicatorPage3.setImageDrawable(if (pageIndex == 2) filledCircle else emptyCircle)
    }
    
    inner class Handler {
        fun goToApp() {
            findNavController().navigate(WelcomeFragmentDirections.actionGoToApp())
        }
        
        val disabledIconAlpha by lazy { requireContext().getFloatFromXml(R.dimen.icon_alpha_disabled) }
        val defaultIconAlpha by lazy { requireContext().getFloatFromXml(R.dimen.icon_alpha_default) }
    }
    
    private val onPageSelectedListener = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            setPageIndicators(position)
        }
    }
    
}
