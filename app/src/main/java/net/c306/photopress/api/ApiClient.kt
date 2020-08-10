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
            
            // Response only logging interceptor
//            builder.addInterceptor { chain ->
//                val request = chain.request()
//                val response = chain.proceed(request)
//                val headers = response.headers
//
//                Timber.i("HTTP Response: $response")
//
//                if (response.body != null) {
//                    val responseBody = response.body!!
//                    val contentLength = responseBody.contentLength()
//
//                    val source = responseBody.source()
//                    source.request(Long.MAX_VALUE) // Buffer the entire body.
//                    var buffer = source.buffer
//
//                    var gzippedLength: Long? = null
//                    if ("gzip".equals(headers["Content-Encoding"], ignoreCase = true)) {
//                        gzippedLength = buffer.size
//                        GzipSource(buffer.clone()).use { gzippedResponseBody ->
//                            buffer = Buffer()
//                            buffer.writeAll(gzippedResponseBody)
//                        }
//                    }
//
//                    val contentType = responseBody.contentType()
//                    val charset: Charset = contentType?.charset(StandardCharsets.UTF_8) ?: StandardCharsets.UTF_8
//
//                    if (!buffer.isProbablyUtf8()) {
//                        Timber.i("")
//                        Timber.i("<-- END HTTP (binary ${buffer.size}-byte body omitted)")
//                        return@addInterceptor response
//                    }
//
//                    if (contentLength != 0L) {
//                        Timber.i("")
//                        Timber.i(buffer.clone().readString(charset))
//                    }
//
//                    if (gzippedLength != null) {
//                        Timber.i("<-- END HTTP (${buffer.size}-byte, $gzippedLength-gzipped-byte body)")
//                    } else {
//                        Timber.i("<-- END HTTP (${buffer.size}-byte body)")
//                    }
//                }
//
//                response
//            }
            
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



///**
// * Returns true if the body in question probably contains human readable text. Uses a small
// * sample of code points to detect unicode control characters commonly used in binary file
// * signatures.
// */
//internal fun Buffer.isProbablyUtf8(): Boolean {
//    try {
//        val prefix = Buffer()
//        val byteCount = size.coerceAtMost(64)
//        copyTo(prefix, 0, byteCount)
//        for (i in 0 until 16) {
//            if (prefix.exhausted()) {
//                break
//            }
//            val codePoint = prefix.readUtf8CodePoint()
//            if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
//                return false
//            }
//        }
//        return true
//    } catch (_: EOFException) {
//        return false // Truncated UTF-8 sequence.
//    }
//}
