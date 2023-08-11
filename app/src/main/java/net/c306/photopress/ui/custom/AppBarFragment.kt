package net.c306.photopress.ui.custom

import androidx.annotation.LayoutRes
import androidx.navigation.fragment.findNavController

abstract class AppBarFragment: BottomNavFragment, AppBarFragmentInterface {
    constructor(): super()
    constructor(@LayoutRes layout: Int): super(layout)
    
    abstract override val myNavId: Int
    
    /**
     * Close fragment
     */
    override fun dismiss() {
        findNavController().popBackStack(myNavId, true)
    }
}