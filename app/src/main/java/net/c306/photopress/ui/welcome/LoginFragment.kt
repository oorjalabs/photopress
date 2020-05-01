package net.c306.photopress.ui.welcome

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
import kotlinx.android.synthetic.main.fragment_login.*
import net.c306.photopress.R
import net.c306.photopress.api.ApiConstants
import net.c306.photopress.api.ApiConstants.ARG_CODE
import net.c306.photopress.api.ApiConstants.ARG_ERROR
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

        webview_login?.apply {
            // Handle link clicks and scroll to progress on page load
            webViewClient = mWebViewClient

            // Enable javascript
            settings.javaScriptEnabled = true

            loadUrl(ApiConstants.AUTHORISE_URL)
        }

        loginViewModel.authComplete.observe(viewLifecycleOwner, Observer {
            // Authorisation complete, navigate back to where we came from
            if (it == true) findNavController().navigate(LoginFragmentDirections.actionPostLogin())
        })

        loginViewModel.authResult.observe(viewLifecycleOwner, Observer {
            if (it == null) return@Observer

            // TODO("If error, show message to user")
        })

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

            // TODO("Tell user about error and ask them to retry.")
            loginViewModel.setAuthResult(
                LoginViewModel.AuthResponse(error = error?.description?.toString() ?: "TODO: Error statement")
            )
            super.onReceivedError(view, request, error)
        }

        // Handle URL changes to check for token in redirect URL
        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            request?.url?.let {

                if (it.host == ApiConstants.AUTH_REDIRECT_HOST && it.path == ApiConstants.AUTH_REDIRECT_PATH) {

                    val parsedResponse = LoginViewModel.AuthResponse(
                        code = it.getQueryParameter(ARG_CODE),
                        error = it.getQueryParameter(ARG_ERROR)
                    )

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
