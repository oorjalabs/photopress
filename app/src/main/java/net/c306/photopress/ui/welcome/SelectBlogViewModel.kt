package net.c306.photopress.ui.welcome

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import net.c306.photopress.R
import net.c306.photopress.api.ApiClient
import net.c306.photopress.api.Blog
import net.c306.photopress.utils.AuthPrefs
import net.c306.photopress.utils.Settings
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

class SelectBlogViewModel(application: Application): AndroidViewModel(application) {
    
    private val apiClient = ApiClient()
    private val applicationContext = application.applicationContext
    
    private val _blogList = MutableLiveData<List<Blog>>()
    val blogList: LiveData<List<Blog>> = _blogList
    
    private val selectedBlog = MutableLiveData<Blog>()
    
    val selectBlogWelcomeSubtitle = Transformations.switchMap(selectedBlog) {
        MutableLiveData<String>().apply {
            value = if (it == null)
                applicationContext.getString(R.string.subtitle_welcome_select_blog)
            else
                applicationContext.getString(R.string.posting_on_blog, it.name)
        }
    }
    
    /**
     * true = have blogs
     * null = not fetched yet
     * false = no blogs (don't re-fetch yet)
     */
    val blogsAvailable = MutableLiveData<Boolean?>().apply { value = null }
    
    
    fun setSelectedBlogId(value: Int) {
        if (value == -1) {
            selectedBlog.value = null
        } else {
            val allBlogs = _blogList.value ?: emptyList()
            selectedBlog.value = allBlogs.find { it.id == value }
        }
        
        Settings.getInstance(applicationContext).setSelectedBlogId(value)
    }
    
    
    init {
        val authPrefs = AuthPrefs(application)
        val savedBlogs = authPrefs.getBlogsList()
        _blogList.value = savedBlogs
        
        if (savedBlogs.isNotEmpty()) {
            blogsAvailable.value = true
            // There is no else, because default is 'null' so blogs can be refreshed
        }
        
        val selectedBlogId = Settings.getInstance(applicationContext).selectedBlogId
        
        selectedBlog.value =
            if (selectedBlogId < 0) null
            else savedBlogs.find { it.id == selectedBlogId }
        
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
                        blogsAvailable.value = false
                        return
                    }
                    
                    Timber.d("Blog info received: ${blogsList.size}")
                    
                    _blogList.value = blogsList
                    blogsAvailable.value = blogsList.isNotEmpty()
                    
                    // Save to storage
                    AuthPrefs(applicationContext)
                        .saveBlogsList(blogsList)
                }
            })
    }

}