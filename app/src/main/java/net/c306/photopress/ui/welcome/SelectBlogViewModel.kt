package net.c306.photopress.ui.welcome

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.mapLatest
import net.c306.photopress.api.Blog
import net.c306.photopress.api.WpService
import net.c306.photopress.utils.AuthPrefs
import net.c306.photopress.utils.Settings
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
internal class SelectBlogViewModel @Inject constructor(
    application: Application,
    private val wpService: WpService,
    private val authPrefs: AuthPrefs,
) : AndroidViewModel(application) {

    private val applicationContext = application.applicationContext

    private val _blogList = MutableLiveData<List<Blog>?>()
    val blogList: LiveData<List<Blog>?> = _blogList

    private val selectedBlog = MutableStateFlow<Blog?>(null)

    val selectBlogWelcomeSubtitle = selectedBlog.mapLatest {
        if (it == null) {
            Title.Default
        } else {
            Title.SelectedBlog(it.name)
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
        wpService
            .listBlogs(Blog.FIELDS_STRING, Blog.OPTIONS_STRING)
            .enqueue(object : Callback<Blog.GetSitesResponse> {
                override fun onFailure(call: Call<Blog.GetSitesResponse>, t: Throwable) {
                    // Error logging in
                    Timber.w(t, "Error fetching blogs!")
                }

                override fun onResponse(
                    call: Call<Blog.GetSitesResponse>,
                    response: Response<Blog.GetSitesResponse>
                ) {
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
                    authPrefs.saveBlogsList(blogsList)
                }
            })
    }

    sealed interface Title {
        data object Default : Title
        data class SelectedBlog(val blogName: String) : Title
    }
}