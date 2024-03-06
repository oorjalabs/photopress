package net.c306.photopress

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.c306.photopress.api.UserDetails
import net.c306.photopress.utils.AppPrefs
import net.c306.photopress.utils.AuthPrefs
import net.c306.photopress.utils.Settings
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
internal class AppViewModel @Inject constructor(
    private val settings: Settings,
    private val appPrefs: AppPrefs,
    private val authPrefs: AuthPrefs,
) : ViewModel() {

    // User details and name
    private val userDetails = MutableLiveData<UserDetails>()

    val userDisplayName = userDetails.switchMap {
        MutableLiveData<String>().apply {
            if (it == null || (it.displayName.isNullOrBlank() && it.username.isNullOrBlank())) {
                return@apply
            }

            value = if (!it.displayName.isNullOrBlank()) it.displayName else it.username
        }
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
            authPrefs.clear()
            settings.clear()
            // If we use a room database, clear that here too

            withContext(Dispatchers.Main) {
                doPostLogoutRestart.value = true
            }
        }
    }

    val doPostLogoutRestart = MutableLiveData<Boolean>()

    private val _showUpdateNotes = MutableLiveData<Boolean>()
    val showUpdateNotes: LiveData<Boolean> = _showUpdateNotes

    // Observer
    private val observer = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        when (key) {
            AuthPrefs.ARG_USER_TOKEN -> _isLoggedIn.value =
                authPrefs.haveAuthToken()

            AuthPrefs.ARG_USER_DETAILS -> userDetails.value =
                authPrefs.getUserDetails()

            AppPrefs.KEY_SHOW_UPDATE_NOTES -> _showUpdateNotes.value = appPrefs.showUpdateNotes
        }
    }


    // Constructor
    init {
        userDetails.value = authPrefs.getUserDetails()
        _isLoggedIn.value = authPrefs.haveAuthToken()

        _showUpdateNotes.value = appPrefs.showUpdateNotes

        authPrefs.observe(observer)
        appPrefs.observe(observer)
    }
}