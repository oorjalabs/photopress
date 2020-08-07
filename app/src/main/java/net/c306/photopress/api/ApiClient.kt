package net.c306.photopress.api

import android.content.Context
import net.c306.photopress.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.util.concurrent.TimeUnit


class ApiClient {
    private lateinit var apiService: ApiService
    
    fun getApiService(context: Context): ApiService {
        
        // Initialize ApiService if not initialized yet
        if (!::apiService.isInitialized) {
            val retrofit = Retrofit.Builder()
                .baseUrl(ApiConstants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okhttpClient(context))
                .build()
            
            apiService = retrofit.create(ApiService::class.java)
        }
        
        return apiService
    }
    
    /**
     * Initialize OkhttpClient with our interceptor
     */
    private fun okhttpClient(context: Context): OkHttpClient {
        
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
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(2, TimeUnit.MINUTES)
            .writeTimeout(2, TimeUnit.MINUTES)
            .addInterceptor(AuthInterceptor(context))
            .build()
    }
}