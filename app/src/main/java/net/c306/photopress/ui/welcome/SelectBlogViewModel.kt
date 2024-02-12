package net.c306.photopress.ui.welcome

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import net.c306.photopress.api.Blog
import net.c306.photopress.api.WpService
import net.c306.photopress.utils.AuthPrefs
import net.c306.photopress.utils.Settings
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
internal class SelectBlogViewModel @Inject constructor(
    application: Application,
    private val wpService: WpService,
    private val authPrefs: AuthPrefs,
    private val settings: Settings,
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

        settings.setSelectedBlogId(value)
    }


    init {
        val authPrefs = AuthPrefs(application)
        val savedBlogs = authPrefs.getBlogsList()
        _blogList.value = savedBlogs

        if (savedBlogs.isNotEmpty()) {
            blogsAvailable.value = true
            // There is no else, because default is 'null' so blogs can be refreshed
        }

        val selectedBlogId = settings.selectedBlogId

        selectedBlog.value =
            if (selectedBlogId < 0) null
            else savedBlogs.find { it.id == selectedBlogId }

    }


    /**
     * Get user details from server
     */
    fun refreshBlogsList() {
        viewModelScope.launch {
            try {
                val blogs = wpService.listBlogs(
                    fields = Blog.FIELDS_STRING,
                    options = Blog.OPTIONS_STRING,
                ).sites

                _blogList.value = blogs
                blogsAvailable.value = blogs.isNotEmpty()

                // Save to storage
                authPrefs.saveBlogsList(blogs)
            } catch (e: IOException) {
                Timber.w(e, "Error fetching blogs!")
            }
        }
    }

    sealed interface Title {
        data object Default : Title
        data class SelectedBlog(val blogName: String) : Title
    }
}