package net.c306.photopress

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import net.c306.photopress.api.ApiClient
import net.c306.photopress.api.AuthPrefs
import net.c306.photopress.api.PostsResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ActivityViewModel(application: Application): AndroidViewModel(application) {

    private val apiClient: ApiClient = ApiClient()

    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean> = _isLoggedIn

    private val _blogSelected = MutableLiveData<Boolean>()
    val blogSelected: LiveData<Boolean> = _blogSelected


    private val observer =
        SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            when (key) {
                AuthPrefs.USER_TOKEN -> _isLoggedIn.value = AuthPrefs(application).haveAuthToken()
            }
        }

    init {
        val authPrefs = AuthPrefs(application)
        _isLoggedIn.value = authPrefs.haveAuthToken()
        authPrefs.observe(observer)
    }

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

                override fun onResponse(
                    call: Call<PostsResponse>,
                    response: Response<PostsResponse>
                ) {
                    // Handle function to display posts
                }
            })
    }
}