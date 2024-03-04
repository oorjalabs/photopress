package net.c306.photopress.ui.welcome

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.c306.photopress.api.Blog
import net.c306.photopress.api.WpService
import net.c306.photopress.utils.AuthPrefs
import net.c306.photopress.utils.Settings
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
internal class SelectBlogViewModel @Inject constructor(
    private val wpService: WpService,
    private val authPrefs: AuthPrefs,
    settings: Settings,
) : ViewModel() {

    private val _blogList = MutableLiveData<List<Blog>?>()
    val blogList: LiveData<List<Blog>?> = _blogList

    private val selectedBlog = MutableStateFlow<Blog?>(null)

    val selectBlogWelcomeSubtitle = settings.selectedBlogIdFlow
        .mapLatest { id ->
            val selectedBlogName = if (id < 0) {
                null
            } else {
                _blogList.value
                    .orEmpty()
                    .find { it.id == id }
                    ?.name
            }

            if (selectedBlogName == null) {
                Title.Default
            } else {
                Title.SelectedBlog(selectedBlogName)
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Title.Default,
        )

    /**
     * true = have blogs
     * null = not fetched yet
     * false = no blogs (don't re-fetch yet)
     */
    val blogsAvailable = MutableLiveData<Boolean?>(null)

    init {
        val savedBlogs = authPrefs.getBlogsList()
        _blogList.value = savedBlogs

        if (savedBlogs.isNotEmpty()) {
            blogsAvailable.value = true
            // There is no else, because default is 'null' so blogs can be refreshed
        }
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
            } catch (e: HttpException) {
                Timber.w(e, "Error fetching blogs!")
            }
        }
    }

    sealed interface Title {
        data object Default : Title
        data class SelectedBlog(val blogName: String) : Title
    }
}