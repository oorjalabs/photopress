package net.c306.photopress.ui.newPost

import android.app.Application
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.provider.MediaStore
import android.text.Html
import android.widget.Toast
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import net.c306.customcomponents.utils.CommonUtils
import net.c306.photopress.R
import net.c306.photopress.api.*
import net.c306.photopress.database.PhotoPressPost
import net.c306.photopress.database.PostImage
import net.c306.photopress.sync.SyncUtils
import net.c306.photopress.sync.SyncUtils.PublishedPost
import net.c306.photopress.utils.AuthPrefs
import net.c306.photopress.utils.UserPrefs
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class NewPostViewModel(application: Application) : AndroidViewModel(application) {
    
    private val applicationContext = application.applicationContext
    
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
    
    internal fun updateState() {
        val title = postTitle.value ?: ""
        val image = postImages.value?.getOrNull(0)
        val blogId = selectedBlogId.value
        val publishedPost = publishedPost.value
        
        _state.value = when {
            blogId == null || blogId < 0 -> State.NO_BLOG_SELECTED
            publishedPost != null        -> State.PUBLISHED
            image == null                -> State.EMPTY
            title.isBlank()              -> State.HAVE_IMAGE
            else                         -> State.READY
        }
    }
    
    val inputsEnabled = Transformations.switchMap(state) {
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
    val selectedBlog = Transformations.switchMap(selectedBlogId) { blogId ->
        val selectedBlog =
            if (blogId == null || blogId < 0) null
            else AuthPrefs(applicationContext)
                .getBlogsList()
                .find { it.id == blogId }
        
        MutableLiveData<Blog?>().apply { value = selectedBlog }
    }
    
    private fun setSelectedBlogId(value: Int) {
        _selectedBlogId.value = value
        
        updateState()
        
        val selectedBlogTags = AuthPrefs(applicationContext).getTagsList()
        val selectedBlogCategories = AuthPrefs(applicationContext).getCategoriesList()
        
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
                AuthPrefs(applicationContext)
                    .saveTagsList(it)
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
        AuthPrefs(applicationContext).saveCategoriesList(list)
        
        // Update on server and sync list
        viewModelScope.launch {
            // Add category on server
            val success = SyncUtils(applicationContext).addCategory(
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
                AuthPrefs(applicationContext)
                    .saveCategoriesList(it)
            }
        }
    }
    
    /**
     * Post Images
     */
    
    private val _postImages = MutableLiveData<MutableList<PostImage>>()
    val postImages: LiveData<MutableList<PostImage>> = _postImages
    
    /**
     * Set or clear selected image Uri(s). Called on selection of images by user, or on `newPost`.
     */
    fun setImageUris(value: List<Uri>?) {
        _postImages.value =
            if (value.isNullOrEmpty()) mutableListOf()
            else value.mapIndexed { index, uri -> PostImage(uri = uri, order = index) }
                .toMutableList()
        
        updateState()
    }
    
    fun addImageUris(newUris: List<Uri>) {
        val list = _postImages.value ?: mutableListOf()
        
        list.addAll(newUris.mapIndexed { index, uri -> PostImage(uri = uri, order = index) })
        
        _postImages.value = list
        updateState()
    }
    
    /**
     * Update [_postImages], usually called after [PostImage.FileDetails] attributes are updated.
     */
    internal fun setPostImages(list: List<PostImage>) {
        _postImages.value = list.toMutableList()
        updateState()
    }
    
    
    /**
     * Update a particular [PostImage] in [_postImages].
     * Usually called after editing image attributes.
     * If image is not in list, it is added at the end.
     */
    internal fun updatePostImage(image: PostImage) {
        val list = _postImages.value ?: mutableListOf()
        
        val index = list.indexOfFirst { it.id == image.id }
        
        // If image in list, update it, else add it
        if (index > -1) list[index] = image
        else list.add(image)
        
        _postImages.value = list.toMutableList()
        updateState()
    }
    
    /**
     * Remove image from [_postImages] list
     */
    internal fun removeImage(image: PostImage) {
        val list = _postImages.value ?: mutableListOf()
        
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
    private val postStatus = MutableLiveData<PhotoPressPost.PhotoPostStatus>()
    
    // Title text
    val postTitle = MutableLiveData<String>()
    
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
    val postCaption = MutableLiveData<String>()
    
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
    private val _scheduledDateTime = MutableLiveData<Long>()
    val scheduledDateTime: LiveData<Long> = _scheduledDateTime
    
    val showTimePicker = MutableLiveData<Boolean>()
    
    private val _scheduleReady = MutableLiveData<Boolean>()
    val scheduleReady: LiveData<Boolean> = _scheduleReady
    
    fun setSchedule(ready: Boolean, dateTime: Long, showTimePicker: Boolean) {
        _scheduledDateTime.value = dateTime
        this.showTimePicker.value = showTimePicker
        _scheduleReady.value = ready
    }
    
    
    // Published post data
    private val _publishedPost = MutableLiveData<PublishedPost>()
    val publishedPost: LiveData<PublishedPost> = _publishedPost
    
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
    }
    
    
    // Observer for changes to selected blog id
    private val observer = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        val userPrefs = UserPrefs(applicationContext)
        when (key) {
            UserPrefs.KEY_SELECTED_BLOG_ID   -> setSelectedBlogId(userPrefs.getSelectedBlogId())
            
            UserPrefs.KEY_PUBLISH_FORMAT     -> useBlockEditor.value = userPrefs.getUseBlockEditor()
            
            UserPrefs.KEY_ADD_FEATURED_IMAGE -> addFeaturedImage.value =
                userPrefs.getAddFeaturedImage()
            
            UserPrefs.KEY_DEFAULT_TAGS       -> defaultTags.value = userPrefs.getDefaultTags()
            
            UserPrefs.KEY_DEFAULT_CATEGORIES -> defaultCategories.value =
                userPrefs.getDefaultCategories()
        }
    }
    
    
    init {
        updateState()
        
        val userPrefs = UserPrefs(applicationContext)
        useBlockEditor.value = userPrefs.getUseBlockEditor()
        addFeaturedImage.value = userPrefs.getAddFeaturedImage()
        defaultTags.value = userPrefs.getDefaultTags()
        defaultCategories.value = userPrefs.getDefaultCategories()
        setSelectedBlogId(userPrefs.getSelectedBlogId())
        userPrefs.observe(observer)
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
        metaCursor?.use { it ->
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
    
    
    private val doPublish = MutableLiveData<Boolean>()
    
    internal fun publishPost(status: PhotoPressPost.PhotoPostStatus) {
        postStatus.value = status
        doPublish.value = true
    }
    
    internal val publishLiveData: LiveData<SyncUtils.PublishLiveData?> =
        doPublish.switchMap<Boolean?, SyncUtils.PublishLiveData?> {
            
            if (it != true) return@switchMap liveData<SyncUtils.PublishLiveData?> { emit(null) }
            
            // Else publish
            val isJetpackBlog = selectedBlog.value?.jetpack
            val blogId = selectedBlogId.value
            val title = postTitle.value
            val images = postImages.value
            val tags = (postTags.value?.split(",")?.toMutableList() ?: mutableListOf())
                .apply { add(applicationContext.getString(R.string.app_post_tag)) }
                .filter { tag -> !tag.isBlank() }
                .distinct()
            val categories = postCategories
                .filter { category -> !category.isBlank() }
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
            
            SyncUtils(applicationContext)
                .publishPostLiveData(
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
            AuthPrefs(applicationContext)
                .saveTagsList(blogTags)
            setBlogTags(blogTags)
            
        }
        
        // Update categories list
        updateCategoriesList()
        
        Timber.d("Blogpost done! $publishedPost")
        
        _publishedPost.value = publishedPost
        setSchedule(false, -1L, false)
        updateState()
    }
    
    
    private data class RefreshTagsResult(
        val errorMessage: String? = null,
        val tags: List<WPTag>? = null
    )
    
    private suspend fun refreshTags() = suspendCoroutine<RefreshTagsResult> { cont ->
        val blogId = selectedBlogId.value?.toString()
        
        if (blogId.isNullOrBlank()) {
            cont.resume(RefreshTagsResult(errorMessage = "No blog selected"))
            return@suspendCoroutine
        }
        
        ApiClient().getApiService(applicationContext)
            .getTagsForSite(blogId)
            .enqueue(object : Callback<WPTag.TagsResponse> {
                override fun onFailure(call: Call<WPTag.TagsResponse>, t: Throwable) {
                    // Error creating post
                    Timber.w(t, "Error fetching tags!")
                    cont.resume(RefreshTagsResult(errorMessage = "Error fetching tags: ${t.localizedMessage}"))
                }
                
                override fun onResponse(
                    call: Call<WPTag.TagsResponse>,
                    response: Response<WPTag.TagsResponse>
                ) {
                    val fetchTagsResponse = response.body()
                    
                    if (fetchTagsResponse == null) {
                        Timber.w("Error updating to published: No blog response received :(")
                        cont.resume(RefreshTagsResult(errorMessage = "Error publishing: No response received"))
                        return
                    }
                    
                    Timber.v("Fetched ${fetchTagsResponse.found} tags")
                    cont.resume(RefreshTagsResult(tags = fetchTagsResponse.tags))
                }
            })
    }
    
    private data class RefreshCategoriesResult(
        val errorMessage: String? = null,
        val categories: List<WPCategory>? = null
    )
    
    private suspend fun refreshCategories() = suspendCoroutine<RefreshCategoriesResult> { cont ->
        val blogId = selectedBlogId.value?.toString()
        
        if (blogId.isNullOrBlank()) {
            cont.resume(RefreshCategoriesResult(errorMessage = "No blog selected"))
            return@suspendCoroutine
        }
        
        ApiClient().getApiService(applicationContext)
            .getCategoriesForSite(blogId)
            .enqueue(object : Callback<WPCategory.GetCategoriesResponse> {
                override fun onFailure(call: Call<WPCategory.GetCategoriesResponse>, t: Throwable) {
                    // Error creating post
                    Timber.w(t, "Error fetching categories!")
                    cont.resume(RefreshCategoriesResult(errorMessage = "Error fetching categories: ${t.localizedMessage}"))
                }
                
                override fun onResponse(
                    call: Call<WPCategory.GetCategoriesResponse>,
                    response: Response<WPCategory.GetCategoriesResponse>
                ) {
                    val fetchCategoriesResponse = response.body()
                    
                    if (fetchCategoriesResponse == null) {
                        Timber.w("Error updating to published: No blog response received :(")
                        cont.resume(RefreshCategoriesResult(errorMessage = "Error publishing: No response received"))
                        return
                    }
                    
                    Timber.v("Fetched ${fetchCategoriesResponse.found} categories")
                    cont.resume(RefreshCategoriesResult(categories = fetchCategoriesResponse.categories))
                }
            })
    }
    
    
    val publishedDialogMessage: String
        get() {
            val published = publishedPost.value ?: return ""
            
            return applicationContext.getString(
                when {
                    published.isDraft                 -> R.string.post_publish_title_post_draft
                    published.post.date.after(Date()) -> R.string.post_publish_title_post_scheduled
                    else                              -> R.string.post_publish_title_post_published
                },
                Html.fromHtml(published.post.title, Html.FROM_HTML_MODE_COMPACT)
            )
        }
    
    
    fun sharePost(post: WPBlogPost) {
        CommonUtils.sendSharingIntent(
            context = applicationContext,
            title = post.url,
            text = post.title
        )
    }
    
    
    fun openPostExternal(post: WPBlogPost) {
        applicationContext.startActivity(CommonUtils.getIntentForUrl(post.url))
    }
    
    
    fun openPostInWordPress(post: WPBlogPost) {
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
    
    
    fun copyPostToClipboard(post: WPBlogPost) {
        CommonUtils.copyToClipboard(applicationContext, "${post.title}\n${post.url}")
    }
    
    
}