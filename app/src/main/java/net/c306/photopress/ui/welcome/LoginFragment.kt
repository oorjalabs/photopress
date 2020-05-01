package net.c306.photopress.ui.welcome

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
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
import kotlinx.android.synthetic.main.fragment_login.*
import net.c306.photopress.R
import net.c306.photopress.api.ApiConstants
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
            // addJavascriptInterface(JSInterface(resources), "IAR")

            loadUrl(ApiConstants.AUTHORISE_URL)

        }

        loginViewModel.authResult.observe(viewLifecycleOwner, Observer {
            if (it == null) return@Observer

            // TODO("If error, show message. Else save token and return to welcome fragment")
        })

    }

    private val mWebViewClient = object : WebViewClient() {

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            Timber.d("Page finished loading... $url")
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
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

                if (it.host == "c306.net" && it.path == "/apps/auth/photopress.html") {
                    val parsedResponse = parseTokenFromUrl(it)

                    Timber.d("Response: $parsedResponse")

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

    private fun parseTokenFromUrl(url: Uri): LoginViewModel.AuthResponse {

        val fragment = url.fragment
            ?: return LoginViewModel.AuthResponse(error = "No data in fragment")

        val params = splitQuery(fragment)

        val authResponse = LoginViewModel.AuthResponse(
            accessToken = params["access_token"],
            expiresIn = params["expires_in"],
            tokenType = params["token_type"],
            siteId = params["site_id"],
            scope = params["scope"],
            error = params["error"]
        )

        return authResponse
    }

    private fun splitQuery(query: String): Map<String, String> {
        val queryPairs: MutableMap<String, String> = mutableMapOf()

        val pairs = query.split("&")

        for (pair in pairs) {
            val idx = pair.indexOf("=")

            val key = if (idx > 0) pair.substring(0, idx) else pair
            val value = if (idx > 0 && pair.length > idx + 1) pair.substring(idx+1) else continue
            queryPairs.putIfAbsent(key, value)
        }
        return queryPairs.toMap()
    }
}
