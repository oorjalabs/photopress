package net.c306.photopress.api

import retrofit2.Call
import retrofit2.http.GET

/**
 * Interface for defining REST request functions
 */
interface ApiService {

    @GET(ApiConstants.POSTS_URL)
    fun fetchPosts(): Call<PostsResponse>
}