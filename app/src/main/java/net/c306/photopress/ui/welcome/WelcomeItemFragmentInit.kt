package net.c306.photopress.ui.welcome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import net.c306.photopress.R

/**
 * Instances of this class are fragments representing a single object in our collection.
 */
class WelcomeItemFragmentInit : Fragment() {
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_welcome_item_init, container, false)
    }
    
}
