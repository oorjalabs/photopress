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
import net.c306.photopress.databinding.FragmentWelcomeItemSelectBlogBinding

/**
 * Instances of this class are fragments representing a single object in our collection.
 */
class WelcomeItemFragmentSelectBlog : Fragment() {
    
    private val appViewModel by activityViewModels<AppViewModel>()
    
    private val selectBlogViewModel by activityViewModels<SelectBlogViewModel>()
    
    private lateinit var binding: FragmentWelcomeItemSelectBlogBinding
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWelcomeItemSelectBlogBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
            selectBlogViewModel = this@WelcomeItemFragmentSelectBlog.selectBlogViewModel
            avm = this@WelcomeItemFragmentSelectBlog.appViewModel
            handler = Handler()
        }
        
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        selectBlogViewModel.blogsAvailable.observe(viewLifecycleOwner, Observer {
            if (it == null) {
                selectBlogViewModel.refreshBlogsList()
            }
        })
        
        appViewModel.blogSelected.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                binding.animationViewDone.playAnimation()
            }
        })
        
    }
    
    
    inner class Handler {
        fun goToApp(@Suppress("UNUSED_PARAMETER") view: View) {
            findNavController().navigate(WelcomeFragmentDirections.actionGoToApp())
        }
        
        fun openBlogChooser(@Suppress("UNUSED_PARAMETER") view: View) {
            findNavController().navigate(WelcomeFragmentDirections.actionSelectBlog())
        }
    }
}
