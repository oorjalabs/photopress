package net.c306.photopress.ui.welcome

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class LoginViewModel(application: Application): AndroidViewModel(application) {

    internal data class AuthResponse(
        val accessToken: String? = null,
        val expiresIn: String? = null,
        val tokenType: String? = null,
        val siteId: String? = null,
        val scope: String? = null,
        val error: String? //error=access_denied
    )

    private val _authResult = MutableLiveData<AuthResponse>()
    internal val authResult: LiveData<AuthResponse> = _authResult

    internal fun setAuthResult(value: AuthResponse) {
        _authResult.value = value
    }

}