package net.c306.photopress.ui.gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.navArgs
import net.c306.photopress.R
import net.c306.photopress.databinding.FragmentFullImageBinding
import net.c306.photopress.ui.custom.AppBarNoBottomNavFragment

/**
 * An example full-screen fragment that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class FullImageFragment : AppBarNoBottomNavFragment() {
    
    @IdRes
    override val myNavId: Int = R.id.fullImageFragment
    
    private var systemUIVisible: Boolean = true
    
    private lateinit var binding: FragmentFullImageBinding
    private val args: FullImageFragmentArgs by navArgs()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFullImageBinding.inflate(inflater, container, false).apply {
            image = args.image
            handler = FragmentHandler()
        }
        return binding.root
    }
    
    
    override fun onResume() {
        super.onResume()
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        
        // Hide system UI after a short delay on start
        binding.root.postDelayed({ hideSystemUI() }, 300)
    }
    
    
    override fun onPause() {
        
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        
        // Show system UI when exiting fragment
        showSystemUI()
        
        super.onPause()
    }
    
    private fun hideSystemUI() {
        activity?.window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_LOW_PROFILE or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        systemUIVisible = false
    }
    
    private fun showSystemUI() {
        (activity as? AppCompatActivity)?.let {
            val uiOptions = it.window.decorView.systemUiVisibility
            
            it.window.decorView.systemUiVisibility =
                uiOptions and
                        View.SYSTEM_UI_FLAG_LOW_PROFILE.inv() and
                        View.SYSTEM_UI_FLAG_FULLSCREEN.inv() and
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE.inv() and
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY.inv() and
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION.inv() and
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION.inv()
        }
        
        systemUIVisible = true
    }
    
    
    @Suppress("UNUSED_PARAMETER")
    inner class FragmentHandler {
        
        fun close(view: View) {
            dismiss()
        }
        
        fun toggle(view: View) {
            if (systemUIVisible) hideSystemUI()
            else showSystemUI()
        }
        
    }
    
}