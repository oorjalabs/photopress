package net.c306.photopress.api

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.c306.photopress.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import javax.inject.Singleton
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

@Module
@InstallIn(SingletonComponent::class)
internal interface WpApiModule {
    companion object {
        @Singleton
        @Provides
        fun provideApiService(authInterceptor: AuthInterceptor): WpService {
            val builder = OkHttpClient.Builder()

            if (BuildConfig.DEBUG) {
                val loggingInterceptor = HttpLoggingInterceptor { Timber.v("HTTP: $it") }
                    .setLevel(HttpLoggingInterceptor.Level.HEADERS)
//                .setLevel(HttpLoggingInterceptor.Level.BODY)
                builder.addInterceptor(loggingInterceptor)
            }

            val okhttpClient = builder
                // Add extended timeouts. Default timeout of 10s is too slow for Jetpack based blogs
                .connectTimeout(30.seconds.toJavaDuration())
                .readTimeout(2.minutes.toJavaDuration())
                .writeTimeout(2.minutes.toJavaDuration())
                .addInterceptor(authInterceptor)
                .build()

            return Retrofit.Builder()
                .baseUrl(ApiConstants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okhttpClient)
                .build()
                .create(WpService::class.java)
        }
    }
}