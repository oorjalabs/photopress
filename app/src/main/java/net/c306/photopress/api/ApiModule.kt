package net.c306.photopress.api

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal interface ApiModule {
    companion object {
        @Singleton
        @Provides
        fun provideApiService(apiClient: ApiClient): ApiService = apiClient.apiService
    }
}