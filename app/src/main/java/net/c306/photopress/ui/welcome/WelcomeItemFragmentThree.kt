package net.c306.photopress.ui.welcome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_welcome_item_3.*
import kotlinx.android.synthetic.main.welcome_progress_indicator.*
import kotlinx.coroutines.*
import net.c306.photopress.ActivityViewModel
import net.c306.photopress.R
import net.c306.photopress.utils.getFloatFromXml

/**
 * Instances of this class are fragments representing a single object in our collection.
 */
class WelcomeItemFragmentThree : Fragment() {

    private val activityViewModel by activityViewModels<ActivityViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_welcome_item_3, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val filledCircle = view.context.getDrawable(R.drawable.ic_circle_filled)
        val emptyCircle = view.context.getDrawable(R.drawable.ic_circle_empty)

        progress_indicator_page_1?.setImageDrawable(emptyCircle)
        progress_indicator_page_2?.setImageDrawable(emptyCircle)
        progress_indicator_page_3?.setImageDrawable(filledCircle)

        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.Default) {
                delay(3000)
            }
            setupComplete()
        }

        button_start?.setOnClickListener {
            findNavController().navigate(WelcomeFragmentDirections.actionWelcomeToPost())
        }

        activityViewModel.isLoggedIn.observe(viewLifecycleOwner, Observer {
            progress_indicator_page_3?.alpha = view.context.getFloatFromXml(
                if (it == true) R.dimen.icon_alpha_default else R.dimen.icon_alpha_disabled
            )
        })
    }

    private fun setupComplete() {
        animation_view_done?.visibility = View.VISIBLE
        animation_view_done?.playAnimation()
        button_start?.visibility = View.VISIBLE
    }
}
