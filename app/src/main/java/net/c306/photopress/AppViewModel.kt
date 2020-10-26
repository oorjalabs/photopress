package net.c306.photopress

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.c306.photopress.api.UserDetails
import net.c306.photopress.utils.AuthPrefs
import net.c306.photopress.utils.Settings
import net.c306.photopress.utils.isPackageInstalled
import timber.log.Timber

class AppViewModel(application: Application) : AndroidViewModel(application) {
    
    private val applicationContext = application.applicationContext
    
    // User details and name
    private val userDetails = MutableLiveData<UserDetails>()
    
    val userDisplayName = Transformations.switchMap(userDetails) {
        MutableLiveData<String>().apply {
            if (it == null || (it.displayName.isNullOrBlank() && it.username.isNullOrBlank())) {
                return@apply
            }
            
            value = if (!it.displayName.isNullOrBlank()) it.displayName else it.username
        }
    }
    
    private val settings by lazy { Settings.getInstance(applicationContext) }
    
    // WordPress app installed
    val isWordPressAppInstalled = MutableLiveData<Boolean>()
    
    // Selected blog
    private val _selectedBlogId = MutableLiveData<Int>()
    val selectedBlogId: LiveData<Int> = _selectedBlogId
    
    private val _blogSelected = MutableLiveData<Boolean>()
    val blogSelected: LiveData<Boolean> = _blogSelected
    
    private fun setSelectedBlog() {
        val selectedBlog = settings.selectedBlogId
        _selectedBlogId.value = selectedBlog
        _blogSelected.value = selectedBlog > -1
    }
    
    
    // Logged in status
    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean> = _isLoggedIn
    
    /**
     * Log out - remove tokens, clear all storage, and restart activity
     */
    fun logout() {
        viewModelScope.launch {
            Timber.i("Logging out...")
            AuthPrefs(applicationContext).clear()
            settings.clear()
            // If we use a room database, clear that here too
            
            withContext(Dispatchers.Main) {
                doPostLogoutRestart.value = true
            }
        }
    }
    
    val doPostLogoutRestart = MutableLiveData<Boolean>()
    
    // Observer
    private val observer = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        when (key) {
            AuthPrefs.ARG_USER_TOKEN       -> _isLoggedIn.value =
                AuthPrefs(applicationContext).haveAuthToken()
            
            AuthPrefs.ARG_USER_DETAILS     -> userDetails.value =
                AuthPrefs(applicationContext).getUserDetails()
            
            Settings.KEY_SELECTED_BLOG_ID -> setSelectedBlog()
        }
    }
    
    
    // Constructor
    init {
        val authPrefs = AuthPrefs(applicationContext)
        
        userDetails.value = authPrefs.getUserDetails()
        _isLoggedIn.value = authPrefs.haveAuthToken()
        setSelectedBlog()
        
        authPrefs.observe(observer)
        settings.observe(observer)
        
        isWordPressAppInstalled.value =
            application.packageManager.isPackageInstalled("org.wordpress.android") == true
    }
    
}