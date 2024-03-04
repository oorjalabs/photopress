package net.c306.photopress.ui.welcome

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import net.c306.photopress.R
import net.c306.photopress.databinding.FragmentWelcomeItemSelectBlogBinding
import net.c306.photopress.utils.viewBinding

/**
 * Instances of this class are fragments representing a single object in our collection.
 */
@AndroidEntryPoint
class WelcomeItemFragmentSelectBlog : Fragment(R.layout.fragment_welcome_item_select_blog) {

    private val viewModel by viewModels<SelectBlogViewModel>()

    private val binding by viewBinding(FragmentWelcomeItemSelectBlogBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        viewModel.blogsAvailable.observe(viewLifecycleOwner) {
            if (it == null) {
                viewModel.refreshBlogsList()
            }
            binding.tvNoBlogMessage.isVisible = it == false
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.isBlogSelected.collectLatest {
                    binding.animationViewDone.isVisible = it
                    if (it) {
                        binding.animationViewDone.playAnimation()
                    }
                    binding.groupSetupComplete.isVisible = it
                    binding.buttonSelectBlog.isVisible = !it
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.selectBlogWelcomeSubtitle.collectLatest {
                    binding.subtitleWelcomeSelectBlog.text = when (it) {
                        SelectBlogViewModel.Title.Default -> {
                            getString(R.string.subtitle_welcome_select_blog)
                        }

                        is SelectBlogViewModel.Title.SelectedBlog -> {
                            getString(R.string.posting_on_blog, it.blogName)
                        }
                    }
                }
            }
        }

        viewModel.blogList.observe(viewLifecycleOwner) {
            binding.buttonSelectBlog.isEnabled = !it.isNullOrEmpty()
        }

        binding.buttonStart.setOnClickListener { goToApp() }
        binding.buttonSelectBlog.setOnClickListener { openBlogChooser() }
    }

    private fun goToApp() {
        findNavController().navigate(WelcomeFragmentDirections.actionGoToApp())
    }

    private fun openBlogChooser() {
        findNavController().navigate(WelcomeFragmentDirections.actionSelectBlog())
    }

    companion object {
        const val SELECT_BLOG_REQUEST_KEY = "selectBlogRequestKey"
        const val SELECTED_BLOG_KEY = "selectedBlogKey"
    }
}