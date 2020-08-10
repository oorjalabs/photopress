package net.c306.photopress.ui

import android.os.Bundle
import android.text.Html
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import net.c306.photopress.R
import net.c306.photopress.databinding.FragmentCreditsBinding
import net.c306.photopress.ui.custom.AppBarFragment

/**
 * Shows credits and thanks for content used
 */
class CreditsFragment : AppBarFragment() {
    
    @IdRes
    override val myNavId: Int = R.id.creditsFragment
    
    private lateinit var binding: FragmentCreditsBinding
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCreditsBinding.inflate(inflater, container, false)
        binding.presenter = Presenter()
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.tvCredits.movementMethod = LinkMovementMethod()
    
        /**
         * Close fragment without saving changes
         */
        binding.toolbar.setNavigationOnClickListener { dismiss() }
    }
    
    inner class Presenter {
        val creditText: Spanned by lazy {
            Html.fromHtml(
                getString(R.string.credits_full_text),
                Html.FROM_HTML_MODE_LEGACY
            )
        }
    }
    
}
