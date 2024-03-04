package net.c306.photopress.ui.welcome

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import net.c306.photopress.utils.Settings
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class WelcomeViewModel @Inject constructor(
    settings: Settings,
): ViewModel() {

    val isBlogSelected = settings.selectedBlogId
        .mapLatest { it > -1 }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = false,
        )

    private val _goToScreen = MutableLiveData<Int>()
    val goToScreen: LiveData<Int> = _goToScreen

    fun setGoToScreen(value: WelcomeFragmentAdapter.Screens?) {
        _goToScreen.value = value?.ordinal
    }
}