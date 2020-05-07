package net.c306.photopress.ui.welcome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_welcome_item_login.*
import kotlinx.android.synthetic.main.welcome_progress_indicator.*
import net.c306.photopress.ActivityViewModel
import net.c306.photopress.R
import net.c306.photopress.utils.getFloatFromXml

/**
 * Instances of this class are fragments representing a single object in our collection.
 */
class WelcomeItemFragmentLogin : Fragment() {

    private val activityViewModel by activityViewModels<ActivityViewModel>()
    private val welcomeViewModel by activityViewModels<WelcomeViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_welcome_item_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val filledCircle = view.context.getDrawable(R.drawable.ic_circle_filled)
        val emptyCircle = view.context.getDrawable(R.drawable.ic_circle_empty)

        progress_indicator_page_1?.setImageDrawable(emptyCircle)
        progress_indicator_page_2?.setImageDrawable(filledCircle)
        progress_indicator_page_3?.setImageDrawable(emptyCircle)

        button_login.setOnClickListener {
            findNavController().navigate(WelcomeFragmentDirections.actionOpenLoginFragment())
        }

        activityViewModel.isLoggedIn.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                // Hide login button and WP/Jetpack text
                button_login?.visibility = View.GONE
                message_wp_or_jetpack?.visibility = View.GONE

                // Show done animation and message
                animation_view_done?.visibility = View.VISIBLE
                animation_view_done?.playAnimation()
                button_go_to_select_blog?.visibility = View.VISIBLE
            }

            progress_indicator_page_3?.alpha = view.context.getFloatFromXml(
                if (it == true) R.dimen.icon_alpha_default else R.dimen.icon_alpha_disabled
            )

        })

        // Show username or display name when logged in
        activityViewModel.userDetails.observe(viewLifecycleOwner, Observer {
            if (it == null || (it.displayName.isNullOrBlank() && it.username.isNullOrBlank())) {
                return@Observer
            }

            val displayName = if (!it.displayName.isNullOrBlank()) it.displayName else it.username

            subtitle_welcome?.text = getString(R.string.connected_as, displayName)
        })
        
        button_go_to_select_blog.setOnClickListener {
            welcomeViewModel.setGoToScreen(WelcomeFragmentAdapter.Screens.SELECT_BLOG)
        }
    }

}
