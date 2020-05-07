package net.c306.photopress.ui.welcome.login

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_login.*
import net.c306.photopress.R
import net.c306.photopress.api.ApiConstants
import net.c306.photopress.api.ApiConstants.ARG_CODE
import net.c306.photopress.api.ApiConstants.ARG_ERROR
import net.c306.photopress.ui.custom.NoBottomNavFragment
import net.c306.photopress.ui.welcome.WelcomeFragmentAdapter
import timber.log.Timber

/**
 * Use to send user to wordpress auth page. Auth redirects to c306 hosted page,
 * from where I read the auth credentials in header/url.
 */
class LoginFragment : NoBottomNavFragment() {
    
    private val loginViewModel by activityViewModels<LoginViewModel>()
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }
    
    
    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        toolbar.setNavigationOnClickListener { returnToWelcome() }
        
        webview_login?.apply {
            // Handle link clicks and scroll to progress on page load
            webViewClient = mWebViewClient
            
            // Enable javascript
            settings.javaScriptEnabled = true
            
            loadUrl(ApiConstants.AUTHORISE_URL)
        }
        
        loginViewModel.authComplete.observe(viewLifecycleOwner, Observer {
            // Authorisation complete, navigate back to where we came from
            if (it == true) returnToWelcome()
        })
        
        loginViewModel.authResult.observe(viewLifecycleOwner, Observer { authResponse ->
            //If error, show message to user
            authResponse?.error?.let {
                Snackbar.make(webview_login, it, Snackbar.LENGTH_LONG).show()
            }
        })
        
        findNavController().navigate(LoginFragmentDirections.actionShowTwoFactorWarning())
    }
    
    
    private fun returnToWelcome() {
        findNavController().navigate(
            LoginFragmentDirections.actionReturnToWelcome(
                WelcomeFragmentAdapter.Screens.LOGIN.screenNumber
            )
        )
    }
    
    private val mWebViewClient = object : WebViewClient() {
        
        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            loading_bar?.hide()
            Timber.d("Page finished loading... $url")
        }
        
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            loading_bar?.show()
            Timber.d("Page started loading... $url")
        }
        
        override fun onReceivedError(
            view: WebView?,
            request: WebResourceRequest?,
            error: WebResourceError?
        ) {
            Timber.d("Received error: $error")
            
            if (request?.isForMainFrame == true) {
                loginViewModel.setAuthResult(
                    LoginViewModel.AuthResponse(
                        error = error?.description?.toString()
                                ?: getString(R.string.message_error_network)
                    )
                )
            }
            
            super.onReceivedError(view, request, error)
        }
        
        // Handle URL changes to check for token in redirect URL
        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            request?.url?.let {
                
                if (it.host == ApiConstants.AUTH_REDIRECT_HOST && it.path == ApiConstants.AUTH_REDIRECT_PATH) {
                    
                    val parsedResponse =
                        LoginViewModel.AuthResponse(
                            code = it.getQueryParameter(ARG_CODE),
                            error = it.getQueryParameter(ARG_ERROR)
                        )
                    
                    // Show loading bar while token is fetched and validated in background
                    // After which view model will close this fragment
                    loading_bar?.show()
                    
                    // Save parsed response to view model
                    loginViewModel.setAuthResult(parsedResponse)
                    
                    // Don't redirect to redirect url
                    return true
                }
                
                Timber.d("redirecting to $it")
            }
            return false
        }
    }

}
