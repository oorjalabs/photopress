package net.c306.photopress.ui.newPost

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.provider.MediaStore
import android.text.Html
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import net.c306.customcomponents.utils.CommonUtils
import net.c306.photopress.R
import net.c306.photopress.api.Blog
import net.c306.photopress.api.WPCategory
import net.c306.photopress.api.WPTag
import net.c306.photopress.api.WpService
import net.c306.photopress.database.PhotoPressPost
import net.c306.photopress.database.PostImage
import net.c306.photopress.sync.SyncUtils
import net.c306.photopress.sync.SyncUtils.PublishedPost
import net.c306.photopress.utils.AuthPrefs
import net.c306.photopress.utils.Settings
import timber.log.Timber
import java.io.IOException
import java.util.*
import javax.inject.Inject

@HiltViewModel
internal class NewPostViewModel @Inject constructor(
    @ApplicationContext
    private val applicationContext: Context,
    private val wpService: WpService,
    private val syncUtils: SyncUtils,
    private val settings: Settings,
    private val authPrefs: AuthPrefs,
) : ViewModel() {

    // Fragment state
    enum class State {
        /** Blog not selected. Can't publish. */
        NO_BLOG_SELECTED,

        /** Image not selected. Can't publish. */
        EMPTY,

        /** Image selected, title text not available. Can't publish. */
        HAVE_IMAGE,

        /** Image & title text are both available. Can publish. */
        READY,

        /** Publishing post. */
        PUBLISHING,

        /** Post. Show link, clear inputs */
        PUBLISHED
    }

    private val _state = MutableLiveData<State>().apply { value = State.EMPTY }
    val state: LiveData<State> = _state

    internal fun setState(newState: State) {
        _state.value = newState
    }

    private val _resetState = MutableSharedFlow<Boolean>()
    val resetState = _resetState.asSharedFlow()

    internal fun updateState() {
        val title = postTitle.value ?: ""
        val image = postImages.value?.getOrNull(0)
        val blogId = selectedBlogId.value
        val publishedPost = publishedPost.value

        _state.value = when {
            blogId == null || blogId < 0 -> State.NO_BLOG_SELECTED
            publishedPost != null -> State.PUBLISHED
            image == null -> State.EMPTY
            title.isBlank() -> State.HAVE_IMAGE
            else -> State.READY
        }
    }

    val inputsEnabled = state.switchMap {
        MutableLiveData<Boolean>().apply {
            value = it != State.NO_BLOG_SELECTED && it != State.PUBLISHING && it != State.PUBLISHED
        }
    }


    // Default post settings
    private val useBlockEditor = MutableLiveData<Boolean>()
    private val addFeaturedImage = MutableLiveData<Boolean>()
    internal val defaultTags = MutableLiveData<String>()
    internal val defaultCategories = MutableLiveData<List<String>>()


    // Selected Blog
    private val _selectedBlogId = MutableLiveData<Int>()
    private val selectedBlogId: LiveData<Int> = _selectedBlogId
    val selectedBlog = selectedBlogId.switchMap { blogId ->
        val selectedBlog =
            if (blogId < 0) {
                null
            } else {
                authPrefs
                    .getBlogsList()
                    .find { it.id == blogId }
            }

        MutableLiveData<Blog?>().apply { value = selectedBlog }
    }

    private fun setSelectedBlogId(value: Int) {
        _selectedBlogId.value = value

        updateState()

        val selectedBlogTags = authPrefs.getTagsList()
        val selectedBlogCategories = authPrefs.getCategoriesList()

        setBlogTags(selectedBlogTags ?: emptyList())
        setBlogCategories(selectedBlogCategories ?: emptyList())

        if (selectedBlogTags == null) updateTagsList()
        if (selectedBlogCategories == null) updateCategoriesList()
    }


    // Selected Blog's Tags
    private val _blogTags = MutableLiveData<List<WPTag>>()
    val blogTags: LiveData<List<WPTag>> = _blogTags

    private fun setBlogTags(list: List<WPTag>) {
        _blogTags.value = list
    }

    private fun updateTagsList() {
        viewModelScope.launch {
            refreshTags().tags?.let {
                setBlogTags(it)
                authPrefs.saveTagsList(it)
            }
        }
    }

    // Selected Blog's Categories
    private val _blogCategories = MutableLiveData<List<WPCategory>>()
    val blogCategories: LiveData<List<WPCategory>> = _blogCategories

    private fun setBlogCategories(list: List<WPCategory>) {
        _blogCategories.value = list
    }

    /**
     * Save category to liveData, storage, and upload to server.
     * Returns updated category list
     */
    internal fun addBlogCategory(category: WPCategory): List<WPCategory> {

        // Update live data
        val list = _blogCategories.value?.toMutableList() ?: mutableListOf()
        list.add(category)
        _blogCategories.value = list

        // Update storage
        authPrefs.saveCategoriesList(list)

        // Update on server and sync list
        viewModelScope.launch {
            // Add category on server
            val success = syncUtils.addCategory(
                selectedBlogId.value ?: return@launch,
                category.name
            )

            if (success) {
                // Refresh categories list from server
                updateCategoriesList()
            }
        }

        return list
    }


    private fun updateCategoriesList() {
        viewModelScope.launch {
            refreshCategories().categories?.let {
                setBlogCategories(it)
                authPrefs.saveCategoriesList(it)
            }
        }
    }

    /**
     * Post Images
     */

    private val _postImages = MutableLiveData<List<PostImage>>(emptyList())
    val postImages: LiveData<List<PostImage>> = _postImages

    /**
     * Set or clear selected image Uri(s). Called on selection of images by user, or on `newPost`.
     */
    fun setImageUris(value: List<Uri>?) {
        _postImages.value =
            if (value.isNullOrEmpty()) {
                emptyList()
            } else {
                value.mapIndexed { index, uri -> PostImage(uri = uri, order = index) }
            }

        updateState()
    }

    fun addImageUris(newUris: List<Uri>) {
        val list = _postImages.value ?: mutableListOf()

        list.toMutableList().apply {
            addAll(newUris.mapIndexed { index, uri -> PostImage(uri = uri, order = index) })
        }

        _postImages.value = list
        updateState()
    }

    /**
     * Update [_postImages], usually called after [PostImage.FileDetails] attributes are updated.
     */
    internal fun setPostImages(list: List<PostImage>) {
        _postImages.value = list
        updateState()
    }


    /**
     * Update a particular [PostImage] in [_postImages].
     * Usually called after editing image attributes.
     * If image is not in list, it is added at the end.
     */
    internal fun updatePostImage(image: PostImage) {
        val list = _postImages.value?.toMutableList() ?: mutableListOf()

        val index = list.indexOfFirst { it.id == image.id }

        // If image in list, update it, else add it
        if (index > -1) {
            list[index] = image
        } else {
            list.add(image)
        }

        _postImages.value = list
        updateState()
    }

    /**
     * Remove image from [_postImages] list
     */
    internal fun removeImage(image: PostImage) {
        val list = _postImages.value?.toMutableList() ?: mutableListOf()

        list.removeIf { it.id == image.id }

        if (image.id == postFeaturedImageId.value) {
            toggleFeaturedImage(null)
        }

        _postImages.value = list
        updateState()
    }

    val imageCount = postImages.switchMap { list ->
        liveData { emit(list.filter { it.fileDetails != null }.size) }
    }


    // Post publishing state
    private val postStatus = MutableLiveData<PhotoPressPost.PhotoPostStatus?>()

    // Title text
    val postTitle = MutableLiveData<String?>()

    // Post tags
    val postTags = MutableLiveData<String>()

    // Post categories
    private val _postCategories = MutableLiveData<List<String>>()
    var postCategories: List<String>
        get() = _postCategories.value ?: listOf()
        set(value) {
            if (_postCategories.value != value)
                _postCategories.value = value
        }

    val postCategoriesDisplayString = _postCategories.switchMap {
        liveData { emit(it.joinToString(", ")) }
    }

    // Post caption (same as image caption in case of single image post)
    val postCaption = MutableLiveData<String?>()

    // Post caption (same as image caption in case of single image post)
    private val _postFeaturedImageId = MutableLiveData<Int>()
    val postFeaturedImageId: LiveData<Int> = _postFeaturedImageId

    fun toggleFeaturedImage(imageId: Int?) {
        _postFeaturedImageId.value =
            if (imageId == null || imageId == _postFeaturedImageId.value) null
            else imageId
    }

    // Image whose attributes are being edited
    val editingImage = MutableLiveData<PostImage?>()


    /**
     * Post scheduling
     */

    // Scheduled post time
    private val _scheduledDateTime = MutableLiveData<Long?>()
    val scheduledDateTime: LiveData<Long?> = _scheduledDateTime

    val showTimePicker = MutableLiveData<Boolean>()

    private val _scheduleReady = MutableLiveData<Boolean>()
    val scheduleReady: LiveData<Boolean> = _scheduleReady

    fun setSchedule(ready: Boolean, dateTime: Long?, showTimePicker: Boolean) {
        _scheduledDateTime.value = dateTime
        this.showTimePicker.value = showTimePicker
        _scheduleReady.value = ready
    }

    fun resetScheduled() {
        _scheduledDateTime.value = null
        this.showTimePicker.value = false
        _scheduleReady.value = false
    }


    // Published post data
    private val _publishedPost = MutableLiveData<PublishedPost?>()
    val publishedPost: LiveData<PublishedPost?> = _publishedPost

    /**
     * Reset view model state for a new post
     */
    fun newPost() {
        _publishedPost.value = null
        postTitle.value = null
        postTags.value = defaultTags.value
        _postCategories.value = defaultCategories.value
        postCaption.value = null
        postStatus.value = null
        toggleFeaturedImage(null)

        setImageUris(null)
        updateState()
        viewModelScope.launch {
            _resetState.emit(true)
        }
    }


    // Observer for changes to selected blog id
    private val observer = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        when (key) {
            Settings.KEY_SELECTED_BLOG_ID -> setSelectedBlogId(settings.selectedBlogId)

            Settings.KEY_PUBLISH_FORMAT -> useBlockEditor.value = settings.useBlockEditor

            Settings.KEY_ADD_FEATURED_IMAGE -> addFeaturedImage.value =
                settings.addFeaturedImage

            Settings.KEY_DEFAULT_TAGS -> defaultTags.value = settings.defaultTags

            Settings.KEY_DEFAULT_CATEGORIES -> defaultCategories.value =
                settings.defaultCategories
        }
    }


    init {
        updateState()

        useBlockEditor.value = settings.useBlockEditor
        addFeaturedImage.value = settings.addFeaturedImage
        defaultTags.value = settings.defaultTags
        defaultCategories.value = settings.defaultCategories
        setSelectedBlogId(settings.selectedBlogId)
        settings.observe(observer)
    }


    /**
     * Gets the file name and mime-type for the given content uri
     */
    internal fun getFileName(uri: Uri): PostImage.FileDetails {

        val projection = arrayOf(
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.MIME_TYPE
        )

        val metaCursor = applicationContext.contentResolver.query(
            uri,
            projection,
            null,
            null,
            null
        )

        var fileName = ""
        var mimeType = ""
        metaCursor?.use {
            if (it.moveToFirst()) {
                fileName = it.getString(0)
                mimeType = it.getString(1)
            }
        }

        return PostImage.FileDetails(
            fileName,
            mimeType
        )
    }


    private val doPublish = MutableLiveData<Boolean?>()

    internal fun publishPost(status: PhotoPressPost.PhotoPostStatus) {
        postStatus.value = status
        doPublish.value = true
    }

    internal val publishLiveData: LiveData<SyncUtils.PublishLiveData?> =
        doPublish.switchMap {

            if (it != true) return@switchMap liveData<SyncUtils.PublishLiveData?> { emit(null) }

            // Else publish
            val isJetpackBlog = selectedBlog.value?.jetpack
            val blogId = selectedBlogId.value
            val title = postTitle.value
            val images = postImages.value
            val tags = (postTags.value?.split(",")?.toMutableList() ?: mutableListOf())
                .apply { add(applicationContext.getString(R.string.app_post_tag)) }
                .filter { tag -> tag.isNotBlank() }
                .distinct()
            val categories = postCategories
                .filter { category -> category.isNotBlank() }
                .distinct()

            if (blogId == null || title.isNullOrBlank() || images.isNullOrEmpty()) {
                Timber.w("Null inputs to publish: blogId: '$blogId', title: '$title', image: '$images'")
                Toast.makeText(applicationContext, "Null inputs to publish :(", Toast.LENGTH_LONG)
                    .show()

                return@switchMap liveData<SyncUtils.PublishLiveData?> { emit(null) }
            }

            val post = PhotoPressPost(
                blogId = blogId,
                postCaption = postCaption.value ?: "",
                // Featured image, or first image if no featured image is set
                postThumbnail = postFeaturedImageId.value ?: images[0].id,
                scheduledTime = scheduledDateTime.value,
                status = postStatus.value ?: PhotoPressPost.PhotoPostStatus.PUBLISH,
                tags = tags,
                categories = categories,
                title = title,
                uploadPending = true
            )

            // Reset published post data
            _publishedPost.value = null
            _state.value = State.PUBLISHING

            syncUtils.publishPostLiveData(
                post = post,
                images = images,
                addFeaturedImage = addFeaturedImage.value,
                useBlockEditor = useBlockEditor.value,
                isJetpackBlog = isJetpackBlog
            )
        }

    internal fun onPublishFinished(publishResult: SyncUtils.PublishPostResponse) {

        doPublish.value = null

        val publishedPost = publishResult.publishedPost!!

        // Add and update returned tags in tags list
        val blogTags = blogTags.value?.toMutableList() ?: mutableListOf()
        val newPostTags = (publishedPost.post.tags).values

        if (newPostTags.isNotEmpty()) {

            // Remove tags that are present in new post's tags
            blogTags.removeIf { tag -> !newPostTags.none { it.id == tag.id } }

            // Add all tags from new post
            blogTags.addAll(newPostTags)

            // Sort alphabetically, ascending
            blogTags.sortBy { it.slug }

            // Save and set updated list
            authPrefs.saveTagsList(blogTags)
            setBlogTags(blogTags)

        }

        // Update categories list
        updateCategoriesList()

        Timber.d("Blogpost done! $publishedPost")

        _publishedPost.value = publishedPost
        resetScheduled()
        updateState()
    }


    private data class RefreshTagsResult(
        val errorMessage: String? = null,
        val tags: List<WPTag>? = null
    )

    private suspend fun refreshTags(): RefreshTagsResult {
        val blogId = selectedBlogId.value?.toString()

        return if (blogId.isNullOrBlank()) {
            RefreshTagsResult(errorMessage = "No blog selected")
        } else {
            val fetchTagsResponse = try {
                wpService.getTagsForSite(blogId)
            } catch (e: IOException) {
                Timber.d(e, "Error fetching tags!")
                null
            }

            if (fetchTagsResponse != null) {
                Timber.v("Fetched ${fetchTagsResponse.found} tags")
                RefreshTagsResult(tags = fetchTagsResponse.tags)
            } else {
                Timber.d("Error updating to published: No blog response received :(")
                RefreshTagsResult(errorMessage = "Error publishing: No response received")
            }
        }
    }

    private data class RefreshCategoriesResult(
        val errorMessage: String? = null,
        val categories: List<WPCategory>? = null
    )

    private suspend fun refreshCategories(): RefreshCategoriesResult {
        val blogId = selectedBlogId.value?.toString()

        return if (blogId.isNullOrBlank()) {
            RefreshCategoriesResult(errorMessage = "No blog selected")
        } else {
            val fetchCategoriesResponse = try {
                wpService.getCategoriesForSite(blogId)
            } catch (e: IOException) {
                Timber.d(e, "Error fetching categories!")
                null
            }

            if (fetchCategoriesResponse != null) {
                Timber.v("Fetched ${fetchCategoriesResponse.found} categories")
                RefreshCategoriesResult(categories = fetchCategoriesResponse.categories)
            } else {
                Timber.d("Error updating to published: No blog response received :(")
                RefreshCategoriesResult(errorMessage = "Error publishing: No response received")
            }
        }
    }


    val publishedDialogMessage: String
        get() {
            val published = publishedPost.value ?: return ""

            return applicationContext.getString(
                when {
                    published.isDraft -> R.string.post_publish_title_post_draft
                    published.post.date.after(Date()) -> R.string.post_publish_title_post_scheduled
                    else -> R.string.post_publish_title_post_published
                },
                Html.fromHtml(published.post.title, Html.FROM_HTML_MODE_COMPACT)
            )
        }


    fun sharePost() {
        publishedPost.value?.post?.let { post ->
            CommonUtils.sendSharingIntent(
                context = applicationContext,
                title = post.url,
                text = post.title
            )
        }
    }


    fun openPostExternal() {
        publishedPost.value?.post?.let { post ->
            applicationContext.startActivity(CommonUtils.getIntentForUrl(post.url))
        }
    }


    fun openPostInWordPress() {
        publishedPost.value?.post?.let { post ->
            applicationContext.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("wordpress://viewpost?blogId=${selectedBlogId.value}&postId=${post.id}")
                    // Uri.parse("wordpress://post?blogId=${selectedBlogId.value}&postId=${post.id}")
                    // Uri.parse("https://wordpress.com/post/${selectedBlogId.value}/${post.id}")
                ).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            )
        }
    }


    fun copyPostToClipboard() {
        publishedPost.value?.post?.let { post ->
            CommonUtils.copyToClipboard(applicationContext, "${post.title}\n${post.url}")
        }
    }
}