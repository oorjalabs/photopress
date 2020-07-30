package net.c306.photopress.ui.welcome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import net.c306.photopress.AppViewModel
import net.c306.photopress.databinding.FragmentWelcomeItemLoginBinding

/**
 * Instances of this class are fragments representing a single object in our collection.
 */
class WelcomeItemFragmentLogin : Fragment() {
    
    private val appViewModel by activityViewModels<AppViewModel>()
    private val welcomeViewModel by activityViewModels<WelcomeViewModel>()
    
    private lateinit var binding: FragmentWelcomeItemLoginBinding
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWelcomeItemLoginBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
            avm = this@WelcomeItemFragmentLogin.appViewModel
            handler = Handler()
        }
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        // Animate done icon when logged in
        appViewModel.isLoggedIn.observe(viewLifecycleOwner, Observer {
            if (it != true) return@Observer
            binding.animationViewDone.playAnimation()
        })
        
    }
    
    inner class Handler {
        fun goToSelectBlog() {
            welcomeViewModel.setGoToScreen(WelcomeFragmentAdapter.Screens.SELECT_BLOG)
        }
        
        fun openLoginFragment() {
            findNavController().navigate(WelcomeFragmentDirections.actionOpenLoginFragment())
        }
    }
}
