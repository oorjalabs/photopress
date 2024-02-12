package net.c306.photopress.api

import net.c306.photopress.utils.AuthPrefs
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * Interceptor to add auth token to requests
 */
internal class AuthInterceptor @Inject constructor(
    private val authPrefs: AuthPrefs,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()

        // If token has been saved, add it to the request
        authPrefs.fetchAuthToken()?.let {
            requestBuilder.addHeader("Authorization", "Bearer $it")
        }

        return chain.proceed(requestBuilder.build())
    }
}