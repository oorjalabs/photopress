package net.c306.photopress.ui.welcome

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import net.c306.photopress.utils.Settings
import javax.inject.Inject

@HiltViewModel
class BlogChooserViewModel @Inject constructor(
    private val settings: Settings,
) : ViewModel() {
    fun setSelectedBlogId(value: Int) {
        settings.setSelectedBlogId(value)
    }
}