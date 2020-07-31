package net.c306.photopress.ui.custom

import androidx.navigation.fragment.findNavController
import net.c306.photopress.utils.hideKeyboard

abstract class AppBarNoBottomNavFragment: NoBottomNavFragment(), AppBarFragmentInterface {
    
    abstract override val myNavId: Int
    
    /**
     * Close fragment
     */
    override fun dismiss() {
        view?.hideKeyboard()
        findNavController().popBackStack(myNavId, true)
    }

}