package net.c306.photopress

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import net.c306.photopress.api.ApiClient
import net.c306.photopress.api.PostsResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ActivityViewModel(application: Application): AndroidViewModel(application) {

    private val apiClient: ApiClient = ApiClient()

    /**
     * Function to fetch posts
     */
    private fun fetchPosts() {

        // Pass the token as parameter
        apiClient.getApiService(getApplication()).fetchPosts()
            .enqueue(object : Callback<PostsResponse> {
                override fun onFailure(call: Call<PostsResponse>, t: Throwable) {
                    // Error fetching posts
                }

                override fun onResponse(call: Call<PostsResponse>, response: Response<PostsResponse>) {
                    // Handle function to display posts
                }
            })
    }
}