package net.c306.photopress.ui.welcome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_welcome_item_3.*
import kotlinx.android.synthetic.main.welcome_progress_indicator.*
import kotlinx.coroutines.*
import net.c306.photopress.R
import net.c306.photopress.ui.welcome.WelcomeFragmentAdapter.Companion.ARG_OBJECT
import net.c306.photopress.utils.getFloatFromXml

/**
 * Instances of this class are fragments representing a single object in our collection.
 */
class WelcomeItemFragment : Fragment() {

    private val myId: Int by lazy {
        arguments?.getInt(ARG_OBJECT) ?: 1
    }

    private val enableThree = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val layout = when (myId) {
            1 -> R.layout.fragment_welcome_item_1
            2 -> R.layout.fragment_welcome_item_2
            3 -> R.layout.fragment_welcome_item_3
            else -> R.layout.fragment_welcome_item_1
        }
        return inflater.inflate(layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        when (myId) {
            3 -> setupThree(view)
            2 -> setupTwo(view)
            else -> setupOne(view)
        }
    }

    private fun setupOne(view: View) {
        val filledCircle = view.context.getDrawable(R.drawable.ic_circle_filled)
        val emptyCircle = view.context.getDrawable(R.drawable.ic_circle_empty)

        progress_indicator_page_1?.setImageDrawable(filledCircle)
        progress_indicator_page_2?.setImageDrawable(emptyCircle)
        progress_indicator_page_3?.setImageDrawable(emptyCircle)

        val progressAlpha = view.context.getFloatFromXml(
            if (enableThree) R.dimen.icon_alpha_default else R.dimen.icon_alpha_disabled
        )
        progress_indicator_page_3?.alpha = progressAlpha

    }

    private fun setupTwo(view: View) {
        val filledCircle = view.context.getDrawable(R.drawable.ic_circle_filled)
        val emptyCircle = view.context.getDrawable(R.drawable.ic_circle_empty)

        progress_indicator_page_1?.setImageDrawable(emptyCircle)
        progress_indicator_page_2?.setImageDrawable(filledCircle)
        progress_indicator_page_3?.setImageDrawable(emptyCircle)

        val progressAlpha = view.context.getFloatFromXml(
            if (enableThree) R.dimen.icon_alpha_default else R.dimen.icon_alpha_disabled
        )
        progress_indicator_page_3?.alpha = progressAlpha

    }

    private fun setupThree(view: View) {
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

    }

    private fun setupComplete() {
        animation_view_done?.visibility = View.VISIBLE
        animation_view_done?.playAnimation()
        button_start?.visibility = View.VISIBLE
    }
}
