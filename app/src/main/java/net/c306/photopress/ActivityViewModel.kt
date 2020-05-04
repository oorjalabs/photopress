package net.c306.photopress

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import net.c306.photopress.api.AuthPrefs
import net.c306.photopress.api.UserDetails

class ActivityViewModel(application: Application): AndroidViewModel(application) {

    private val _userDetails = MutableLiveData<UserDetails>()
    val userDetails: LiveData<UserDetails> = _userDetails

    private val _selectedBlogId = MutableLiveData<Int>()
    val selectedBlogId: LiveData<Int> = _selectedBlogId

    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean> = _isLoggedIn

    private val _blogSelected = MutableLiveData<Boolean>()
    val blogSelected: LiveData<Boolean> = _blogSelected

    private val observer =
        SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            when (key) {
                AuthPrefs.ARG_USER_TOKEN -> _isLoggedIn.value = AuthPrefs(application).haveAuthToken()

                AuthPrefs.ARG_USER_DETAILS -> _userDetails.value = AuthPrefs(application).getUserDetails()

                AuthPrefs.ARG_SELECTED_BLOG_ID -> {
                    val selectedBlog = AuthPrefs(application).getSelectedBlogId()
                    _selectedBlogId.value = selectedBlog
                    _blogSelected.value = selectedBlog > -1
                }
            }
        }

    init {
        val authPrefs = AuthPrefs(application)
        
        _userDetails.value = authPrefs.getUserDetails()
        _isLoggedIn.value = authPrefs.haveAuthToken()
        val selectedBlog = authPrefs.getSelectedBlogId()
        _selectedBlogId.value = selectedBlog
        _blogSelected.value = selectedBlog > -1

        authPrefs.observe(observer)
    }

}