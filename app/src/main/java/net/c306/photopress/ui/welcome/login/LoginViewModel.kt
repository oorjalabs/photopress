package net.c306.photopress.ui.welcome.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import net.c306.photopress.R
import net.c306.photopress.api.TokenRequest
import net.c306.photopress.api.UserDetails
import net.c306.photopress.api.WpService
import net.c306.photopress.utils.AuthPrefs
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
internal class LoginViewModel @Inject constructor(
    application: Application,
    private val authPrefs: AuthPrefs,
    private val wpService: WpService,
) : AndroidViewModel(application) {

    private val applicationContext = application.applicationContext

    internal data class AuthResponse(
        val accessToken: String? = null,
        val expiresIn: String? = null,
        val tokenValidated: Boolean = false,
        val tokenType: String? = null,
        val siteId: String? = null,
        val scope: String? = null,
        val code: String? = null,
        val blogUrl: String? = null,
        val userId: String? = null,
        val error: String? //error=access_denied
    )

    private val _authComplete = MutableLiveData<Boolean>()
    internal val authComplete: LiveData<Boolean> = _authComplete

    private val _authResult = MutableLiveData<AuthResponse>()
    internal val authResult: LiveData<AuthResponse> = _authResult

    internal fun setAuthResult(authResponse: AuthResponse) {
        _authResult.value = authResponse

        when {

            !authResponse.code.isNullOrBlank() -> {
                // If authResponse has `code`, then exchange it for token.
                Timber.d("Auth code received, exchange it for token")
                viewModelScope.launch { getToken(authResponse.code) }
            }

            !authResponse.accessToken.isNullOrBlank() &&
                !authResponse.tokenValidated &&
                !authResponse.error.isNullOrBlank() -> {
                // Token is invalid. Discard it and show message to user to re-authenticate
                Timber.d("Invalid token")
                // TODO("Prompt user to login again")
            }

            !authResponse.accessToken.isNullOrBlank() &&
                !authResponse.tokenValidated -> {
                // AuthResponse has `token` but not validated; Validate token.
                Timber.d("Token received, validate it")
                viewModelScope.launch {
                    validateAuthToken(authResponse.accessToken)
                }
            }

            !authResponse.accessToken.isNullOrBlank() -> {
                // Token validated, save it to storage
                Timber.d("Token validated, save it")
                authPrefs.saveAuthToken(authResponse.accessToken)
                viewModelScope.launch {
                    getToKnowMe()
                }
                _authComplete.value = true
            }
        }
    }


    /**
     * Get access token using auth code
     */
    private suspend fun getToken(code: String) {
        val tokenRequest = TokenRequest(code = code)

        val tokenResponse = try {
            wpService.getToken(tokenRequest.toFieldMap())
        } catch (e: IOException) {
            Timber.w(e, "Error getting token!")
            setAuthResult(
                AuthResponse(
                    error = applicationContext.getString(R.string.message_error_no_auth_token)
                )
            )
            null
        } catch (e: HttpException) {
            Timber.w(e, "Error getting token!")
            setAuthResult(
                AuthResponse(
                    error = applicationContext.getString(R.string.message_error_no_auth_token)
                )
            )
            null
        }

        if (tokenResponse?.accessToken != null) {
            // sessionManager.saveAuthToken(loginResponse.authToken)
            setAuthResult(
                AuthResponse(
                    accessToken = tokenResponse.accessToken,
                    tokenType = tokenResponse.tokenType,
                    siteId = tokenResponse.blogId,
                    blogUrl = tokenResponse.blogUrl,
                    error = null
                )
            )
        } else {
            // Error logging in
            Timber.w("Got invalid response getting token: $tokenResponse")
            setAuthResult(
                AuthResponse(
                    error = applicationContext.getString(R.string.message_error_no_auth_token)
                )
            )
        }
    }

    /**
     * Validate auth token with server
     */
    private suspend fun validateAuthToken(token: String) {

        val tokenResponse = try {
            wpService.validateToken(token = token)
        } catch (e: IOException) {
            Timber.d(e, "Error getting token!")
            setAuthResult(
                AuthResponse(
                    error = applicationContext.getString(R.string.message_error_network)
                )
            )
            null
        } catch (e: HttpException) {
            Timber.d(e, "Error getting token!")
            setAuthResult(
                AuthResponse(
                    error = applicationContext.getString(R.string.message_error_network)
                )
            )
            null
        }

        if (tokenResponse != null && tokenResponse.error.isNullOrBlank()) {
            Timber.d("Token verified. Yay! $tokenResponse")
            setAuthResult(
                AuthResponse(
                    accessToken = token,
                    tokenValidated = true,
                    userId = tokenResponse.userId,
                    siteId = tokenResponse.blogId,
                    error = null
                )
            )
        } else {
            Timber.w("Token not validated")
            setAuthResult(
                AuthResponse(
                    accessToken = token,
                    tokenValidated = false,
                    error = applicationContext.getString(R.string.message_error_token_validation)
                )
            )
        }
    }

    /**
     * Get user details from server
     */
    private suspend fun getToKnowMe() {
        try {
            authPrefs.saveUserDetails(wpService.aboutMe(UserDetails.FIELD_STRING))
        } catch (e: IOException) {
            Timber.d(e, "Error getting user details")
        } catch (e: HttpException) {
            Timber.d(e, "Error getting user details")
        }
    }

}