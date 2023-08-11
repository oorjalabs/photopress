package net.c306.photopress.ui

import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.annotation.IdRes
import net.c306.photopress.R
import net.c306.photopress.databinding.FragmentCreditsBinding
import net.c306.photopress.ui.custom.AppBarFragment
import net.c306.photopress.utils.viewBinding

/**
 * Shows credits and thanks for content used
 */
class CreditsFragment : AppBarFragment(R.layout.fragment_credits) {
    
    @IdRes
    override val myNavId: Int = R.id.creditsFragment
    
    private val binding by viewBinding { FragmentCreditsBinding.bind(it) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.tvCredits.movementMethod = LinkMovementMethod()
        
        /**
         * Close fragment without saving changes
         */
        binding.toolbar.setNavigationOnClickListener { dismiss() }
        
        binding.tvCredits.text = Html.fromHtml(
            getString(R.string.credits_full_text),
            Html.FROM_HTML_MODE_LEGACY
        ) 
    }
}
