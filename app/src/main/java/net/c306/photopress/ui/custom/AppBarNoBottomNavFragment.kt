package net.c306.photopress.ui.custom

import androidx.annotation.LayoutRes
import androidx.navigation.fragment.findNavController
import net.c306.customcomponents.utils.hideKeyboard

abstract class AppBarNoBottomNavFragment: NoBottomNavFragment, AppBarFragmentInterface {
    
    constructor(): super()
    constructor(@LayoutRes layout: Int): super(layout)
    
    abstract override val myNavId: Int
    
    /**
     * Close fragment
     */
    override fun dismiss() {
        view?.hideKeyboard()
        findNavController().popBackStack(myNavId, true)
    }

}