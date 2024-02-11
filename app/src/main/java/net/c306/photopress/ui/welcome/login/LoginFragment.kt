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
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import net.c306.photopress.R
import net.c306.photopress.api.ApiConstants
import net.c306.photopress.api.ApiConstants.ARG_CODE
import net.c306.photopress.api.ApiConstants.ARG_ERROR
import net.c306.photopress.databinding.FragmentLoginBinding
import net.c306.photopress.ui.custom.NoBottomNavFragment
import net.c306.photopress.ui.welcome.WelcomeFragmentAdapter
import timber.log.Timber

/**
 * Use to send user to wordpress auth page. Auth redirects to c306 hosted page,
 * from where I read the auth credentials in header/url.
 */
class LoginFragment : NoBottomNavFragment() {

    private val loginViewModel by activityViewModels<LoginViewModel>()

    private var _binding: FragmentLoginBinding? = null
    // This property is only valid between onCreateView and
// onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener { returnToWelcome() }

        with(binding.webviewLogin) {
            // Handle link clicks and scroll to progress on page load
            webViewClient = mWebViewClient

            // Enable javascript
            settings.javaScriptEnabled = true

            loadUrl(ApiConstants.AUTHORISE_URL)
        }

        loginViewModel.authComplete.observe(viewLifecycleOwner) {
            // Authorisation complete, navigate back to where we came from
            if (it == true) returnToWelcome()
        }

        loginViewModel.authResult.observe(viewLifecycleOwner) { authResponse ->
            //If error, show message to user
            authResponse?.error?.let {
                Snackbar.make(binding.webviewLogin, it, Snackbar.LENGTH_LONG).show()
            }
        }

        findNavController().navigate(LoginFragmentDirections.actionShowTwoFactorWarning())
    }


    private fun returnToWelcome() {
        findNavController().navigate(
            LoginFragmentDirections.actionReturnToWelcome(
                WelcomeFragmentAdapter.Screens.LOGIN.ordinal
            )
        )
    }

    private val mWebViewClient = object : WebViewClient() {

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            binding.loadingBar.hide()
            Timber.d("Page finished loading... $url")
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            binding.loadingBar.show()
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
                    binding.loadingBar.show()

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