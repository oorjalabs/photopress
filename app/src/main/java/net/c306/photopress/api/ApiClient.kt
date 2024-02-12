package net.c306.photopress.api

import net.c306.photopress.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

@Singleton
internal class ApiClient @Inject constructor(
    private val authInterceptor: AuthInterceptor,
) {
    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(ApiConstants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okhttpClient())
            .build()
            .create(ApiService::class.java)
    }

    /**
     * Initialize OkhttpClient with our interceptor
     */
    private fun okhttpClient(): OkHttpClient {

        val builder = OkHttpClient.Builder()

        if (BuildConfig.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
                override fun log(message: String) {
                    Timber.v("HTTP: $message")
                }
            })

            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS)
//            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
            builder.addInterceptor(loggingInterceptor)
        }

        return builder
            // Add extended timeouts. Default timeout of 10s is too slow for Jetpack based blogs
            .connectTimeout(30.seconds.toJavaDuration())
            .readTimeout(2.minutes.toJavaDuration())
            .writeTimeout(2.minutes.toJavaDuration())
            .addInterceptor(authInterceptor)
            .build()
    }
}