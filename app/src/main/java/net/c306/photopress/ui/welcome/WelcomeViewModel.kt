package net.c306.photopress.ui.welcome

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class WelcomeViewModel: ViewModel() {

    private val _openLoginScreen = MutableLiveData<Boolean>()
    val openLoginScreen: LiveData<Boolean> = _openLoginScreen

    fun setOpenLoginScreen(value: Boolean) {
        _openLoginScreen.value = value
    }
}