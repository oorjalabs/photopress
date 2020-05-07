package net.c306.photopress.ui.custom

interface AppBarFragmentInterface {
    abstract val myNavId: Int
    
    /**
     * Close fragment
     */
    abstract fun dismiss()
}