package net.c306.photopress.ui.welcome

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import net.c306.photopress.AppViewModel
import net.c306.photopress.R
import net.c306.photopress.core.extensions.viewBinding
import net.c306.photopress.databinding.FragmentWelcomeItemLoginBinding
import net.c306.photopress.ui.custom.NoBottomNavFragment

/**
 * Instances of this class are fragments representing a single object in our collection.
 */
class WelcomeItemFragmentLogin : NoBottomNavFragment(R.layout.fragment_welcome_item_login) {

    private val appViewModel by activityViewModels<AppViewModel>()
    private val viewModel by activityViewModels<WelcomeViewModel>()

    private val binding by viewBinding(FragmentWelcomeItemLoginBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        // Animate done icon when logged in
        appViewModel.isLoggedIn.observe(viewLifecycleOwner) {
            binding.animationViewDone.isVisible = it
            if (it == true) {
                binding.animationViewDone.playAnimation()
            }
            binding.buttonGoToSelectBlog.isVisible = it
            binding.buttonLogin.isVisible = !it
            binding.messageWpOrJetpack.isVisible = !it
        }

        appViewModel.userDisplayName.observe(viewLifecycleOwner) {
            binding.subtitleWelcome.text = if (it == null) {
                getString(R.string.subtitle_login_to_wordpress)
            } else {
                getString(R.string.connected_as, it)
            }
        }

        binding.buttonGoToSelectBlog.setOnClickListener {
            goToSelectBlog()
        }

        binding.buttonLogin.setOnClickListener {
            openLoginFragment()
        }
    }

    private fun goToSelectBlog() {
        viewModel.setGoToScreen(WelcomeFragmentAdapter.Screens.SELECT_BLOG)
    }

    private fun openLoginFragment() {
        findNavController().navigate(WelcomeFragmentDirections.actionOpenLoginFragment())
    }
}