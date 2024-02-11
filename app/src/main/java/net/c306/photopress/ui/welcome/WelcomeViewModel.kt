package net.c306.photopress.ui.welcome

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class WelcomeViewModel: ViewModel() {

    private val _goToScreen = MutableLiveData<Int>()
    val goToScreen: LiveData<Int> = _goToScreen

    fun setGoToScreen(value: WelcomeFragmentAdapter.Screens?) {
        _goToScreen.value = value?.ordinal
    }

}