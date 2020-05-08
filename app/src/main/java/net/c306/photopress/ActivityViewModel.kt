package net.c306.photopress

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import net.c306.photopress.api.UserDetails
import net.c306.photopress.utils.AuthPrefs
import net.c306.photopress.utils.UserPrefs

class ActivityViewModel(application: Application): AndroidViewModel(application) {
    
    private val applicationContext = application.applicationContext

    private val _userDetails = MutableLiveData<UserDetails>()
    val userDetails: LiveData<UserDetails> = _userDetails
    
    val userDisplayName = Transformations.switchMap(userDetails) {
        MutableLiveData<String>().apply {
            if (it == null || (it.displayName.isNullOrBlank() && it.username.isNullOrBlank())) {
                return@apply
            }
    
            value = if (!it.displayName.isNullOrBlank()) it.displayName else it.username
        }
    }

    private val _selectedBlogId = MutableLiveData<Int>()
    val selectedBlogId: LiveData<Int> = _selectedBlogId
    
    private fun setSelectedBlog() {
        val selectedBlog = UserPrefs(applicationContext)
            .getSelectedBlogId()
        _selectedBlogId.value = selectedBlog
        _blogSelected.value = selectedBlog > -1
    }

    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean> = _isLoggedIn

    private val _blogSelected = MutableLiveData<Boolean>()
    val blogSelected: LiveData<Boolean> = _blogSelected

    private val observer =
        SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            when (key) {
                AuthPrefs.ARG_USER_TOKEN   -> _isLoggedIn.value =
                        AuthPrefs(applicationContext).haveAuthToken()
                
                AuthPrefs.ARG_USER_DETAILS -> _userDetails.value =
                        AuthPrefs(applicationContext).getUserDetails()
                
                UserPrefs.KEY_SELECTED_BLOG_ID                                          -> setSelectedBlog()
            }
        }

    init {
        val authPrefs = AuthPrefs(applicationContext)
        
        _userDetails.value = authPrefs.getUserDetails()
        _isLoggedIn.value = authPrefs.haveAuthToken()
        setSelectedBlog()
        
        authPrefs.observe(observer)
        UserPrefs(applicationContext).observe(observer)
    }

}