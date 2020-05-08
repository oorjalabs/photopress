package net.c306.photopress.ui.welcome.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import net.c306.photopress.R
import net.c306.photopress.api.ApiClient
import net.c306.photopress.api.ApiService.GetTokenResponse
import net.c306.photopress.api.ApiService.ValidateTokenResponse
import net.c306.photopress.api.TokenRequest
import net.c306.photopress.api.UserDetails
import net.c306.photopress.utils.AuthPrefs
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

class LoginViewModel(application: Application): AndroidViewModel(application) {
    
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
    
    private val apiClient = ApiClient()
    
    internal fun setAuthResult(authResponse: AuthResponse) {
        _authResult.value = authResponse
        
        when {
            
            !authResponse.code.isNullOrBlank() -> {
                // If authResponse has `code`, then exchange it for token.
                Timber.d("Auth code received, exchange it for token")
                getToken(authResponse.code)
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
                validateAuthToken(authResponse.accessToken)
            }
            
            !authResponse.accessToken.isNullOrBlank() -> {
                // Token validated, save it to storage
                Timber.d("Token validated, save it")
                AuthPrefs(getApplication())
                    .saveAuthToken(authResponse.accessToken)
                getToKnowMe()
                _authComplete.value = true
            }
        }
    }
    
    
    
    /**
     * Get access token using auth code
     */
    private fun getToken(code: String) {
        val tokenRequest = TokenRequest(code = code)
        
        apiClient.getApiService(getApplication())
            .getToken(tokenRequest.toFieldMap())
            .enqueue(object : Callback<GetTokenResponse> {
                override fun onFailure(call: Call<GetTokenResponse>, t: Throwable) {
                    // Error logging in
                    Timber.w(t, "Error getting token!")
                    setAuthResult(
                        AuthResponse(
                            error = applicationContext.getString(R.string.message_error_no_auth_token)
                        )
                    )
                }
                
                override fun onResponse(call: Call<GetTokenResponse>, response: Response<GetTokenResponse>) {
                    val tokenResponse = response.body()
                    
                    if (tokenResponse?.accessToken != null) {
                        // sessionManager.saveAuthToken(loginResponse.authToken)
                        Timber.d("Got token: $tokenResponse")
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
            })
    }
    
    /**
     * Validate auth token with server
     */
    private fun validateAuthToken(token: String) {
        
        apiClient.getApiService(getApplication())
            .validateToken(token = token)
            .enqueue(object : Callback<ValidateTokenResponse> {
                override fun onFailure(call: Call<ValidateTokenResponse>, t: Throwable) {
                    // Error logging in
                    Timber.w(t, "Error getting token!")
                    setAuthResult(
                        AuthResponse(
                            error = applicationContext.getString(R.string.message_error_network)
                        )
                    )
                }
                
                override fun onResponse(call: Call<ValidateTokenResponse>, response: Response<ValidateTokenResponse>) {
                    val tokenResponse = response.body()
                    
                    if (tokenResponse == null || !tokenResponse.error.isNullOrBlank()) {
                        Timber.w("Token not validated")
                        setAuthResult(
                            AuthResponse(
                                accessToken = token,
                                tokenValidated = false,
                                error = applicationContext.getString(R.string.message_error_token_validation)
                            )
                        )
                        return
                    }
                    
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
                    
                }
            })
    }
    
    /**
     * Get user details from server
     */
    private fun getToKnowMe() {
        
        apiClient.getApiService(getApplication())
            .aboutMe(UserDetails.FIELD_STRING)
            .enqueue(object : Callback<UserDetails> {
                override fun onFailure(call: Call<UserDetails>, t: Throwable) {
                    // Error logging in
                    Timber.w(t, "Error getting token!")
                }
                
                override fun onResponse(call: Call<UserDetails>, response: Response<UserDetails>) {
                    val userDetails = response.body()
                    
                    if (userDetails == null) {
                        Timber.w("No user info recovered :(")
                        return
                    }
                    
                    Timber.d("User info received: $userDetails")
                    
                    AuthPrefs(getApplication())
                        .saveUserDetails(userDetails)
                }
            })
    }

}