package net.c306.photopress.ui.welcome

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import net.c306.photopress.UserPrefs
import net.c306.photopress.api.ApiClient
import net.c306.photopress.api.AuthPrefs
import net.c306.photopress.api.Blog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

class SelectBlogViewModel(application: Application): AndroidViewModel(application) {

    private val apiClient = ApiClient()
    private val applicationContext = application.applicationContext

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
        
        UserPrefs(applicationContext).setSelectedBlogId(value)
    }

    init {
        val authPrefs = AuthPrefs(application)
        _blogList.value = authPrefs.getBlogsList()

        val selectedBlogId = UserPrefs(applicationContext).getSelectedBlogId()
        
        if (selectedBlogId < 0) {
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

        apiClient.getApiService(applicationContext)
            .listBlogs(Blog.FIELDS_STRING, Blog.OPTIONS_STRING)
            .enqueue(object : Callback<Blog.GetSitesResponse> {
                override fun onFailure(call: Call<Blog.GetSitesResponse>, t: Throwable) {
                    // Error logging in
                    Timber.w(t, "Error fetching blogs!")
                }

                override fun onResponse(call: Call<Blog.GetSitesResponse>, response: Response<Blog.GetSitesResponse>) {
                    val blogsList = response.body()?.sites

                    if (blogsList == null) {
                        Timber.w("No blog info recovered :(")
                        return
                    }

                    Timber.d("Blog info received: ${blogsList.size}")

                    _blogList.value = blogsList

                    // Save to storage
                    AuthPrefs(applicationContext)
                        .saveBlogsList(blogsList)
                }
            })
    }

}