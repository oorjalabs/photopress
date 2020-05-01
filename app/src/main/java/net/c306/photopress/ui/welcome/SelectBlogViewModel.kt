package net.c306.photopress.ui.welcome

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import net.c306.photopress.api.ApiClient
import net.c306.photopress.api.ApiService
import net.c306.photopress.api.AuthPrefs
import net.c306.photopress.api.Blog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

class SelectBlogViewModel(application: Application): AndroidViewModel(application) {

    private val apiClient = ApiClient()

    private val _blogList = MutableLiveData<List<Blog>>()
    val blogList: LiveData<List<Blog>> = _blogList

    private val _selectedBlog = MutableLiveData<Blog>()
    val selectedBlog: LiveData<Blog> = _selectedBlog

    fun setSelectedBlogId(value: Int) {
        if (value == -1) {
            _selectedBlog.value = null
        } else {
            val allBlogs = _blogList.value ?: emptyList()
            _selectedBlog.value = allBlogs.find { it.id == value }
        }

        AuthPrefs(getApplication()).setSelectedBlogId(value)
    }

    init {
        val authPrefs = AuthPrefs(application)
        _blogList.value = authPrefs.getBlogsList()

        val selectedBlogId = authPrefs.getSelectedBlogId()

        if (selectedBlogId == -1) {
            _selectedBlog.value = null
        } else {
            val allBlogs = _blogList.value ?: emptyList()
            _selectedBlog.value = allBlogs.find { it.id == selectedBlogId }
        }

    }

    /**
     * Get user details from server
     */
    internal fun refreshBlogsList() {

        apiClient.getApiService(getApplication())
            .listBlogs("")
            .enqueue(object : Callback<ApiService.SitesResponse> {
                override fun onFailure(call: Call<ApiService.SitesResponse>, t: Throwable) {
                    // Error logging in
                    Timber.w(t, "Error fetching blogs!")
                }

                override fun onResponse(call: Call<ApiService.SitesResponse>, response: Response<ApiService.SitesResponse>) {
                    val blogsList = response.body()?.sites

                    if (blogsList == null) {
                        Timber.w("No blog info recovered :(")
                        return
                    }

                    Timber.d("Blog info received: ${blogsList.size}")

                    _blogList.value = blogsList

                    // Save to storage
                    AuthPrefs(getApplication())
                        .saveBlogsList(blogsList)
                }
            })
    }

}