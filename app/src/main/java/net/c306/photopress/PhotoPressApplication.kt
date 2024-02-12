package net.c306.photopress

import android.app.Application
import android.os.StrictMode
import dagger.hilt.android.HiltAndroidApp
import net.c306.customcomponents.utils.CommonUtils
import net.c306.photopress.utils.AppPrefs
import net.c306.photopress.utils.Settings
import timber.log.Timber
import java.util.*
import java.util.regex.Pattern
import javax.inject.Inject

@HiltAndroidApp
class PhotoPressApplication : Application() {

    @Inject
    lateinit var settings: Settings

    @Inject
    lateinit var appPrefs: AppPrefs

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(UpdatedTimberDebugTree())
            StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectAll()
//                .detectNetwork()   // or .detectAll() for all detectable problems
                .penaltyLog()
                .build())
            StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .build())
        }

        doAppUpgrades()

    }

    /**
     * Performs actions to be completed on new install or upgrade.
     * Set `show update notes` if required.
     */
    private fun doAppUpgrades() {

        // Get last stored app version
        val savedAppVersion = appPrefs.appVersion

        when {
            // New install
            savedAppVersion == null                    -> {
                // Mark update notes available
                appPrefs.setShowUpdateNotes(true)
                // Set install date
                // Maybe set install date on first sign-in. (Only if there's no previous install date)
                appPrefs.setFirstUseTimestamp(Date().time)
            }

            // App updated
            BuildConfig.VERSION_CODE > savedAppVersion -> {

                val previousNamedVersion = appPrefs.previousNamedVersion
                val namedVersion = CommonUtils.getNamedVersion(BuildConfig.VERSION_NAME)

                // If this is first open since app update, set show upgrade notice
                if (namedVersion > previousNamedVersion) {
                    appPrefs.setShowUpdateNotes(BuildConfig.SHOW_WHATS_NEW)
                    appPrefs.savePreviousNamedVersion(namedVersion)
                }

                // Do other upgrades
                // ...

                // Update stored values for previous list preference or upgradedListPreference from
                // string to set<string>
                if (savedAppVersion < APP_VERSION_UPGRADED_CUSTOM_PREFERENCES) {
                    Timber.v("OnUpgrade: Upgrade custom preferences")
                    settings.upgradeCustomPreferences()
                }

                if (savedAppVersion < APP_VERSION_SAVE_INSTALL_DATE) {
                    // Save install date
                    appPrefs.setFirstUseTimestamp(Date().time)
                }

            }

            // Not an upgrade or install, do nothing
            else                                       -> return
        }

        // Save updated app version
        appPrefs.saveAppVersion(BuildConfig.VERSION_CODE)
    }


    private class UpdatedTimberDebugTree : Timber.DebugTree() {

        // Copied from DebugTree code
        override fun createStackElementTag(element: StackTraceElement): String {
            var tag = LOG_TAG + element.className.substringAfterLast('.')
            val m = ANONYMOUS_CLASS.matcher(tag)
            if (m.find()) {
                tag = m.replaceAll("")
            }

            val trimmedTag = if (tag.length <= MAX_TAG_LENGTH)
                tag
            else
                tag.substring(0, MAX_TAG_LENGTH)

            return "$trimmedTag:${element.lineNumber}"
        }

        companion object {
            private const val LOG_TAG = "PHOTOPRESS "
            private const val MAX_TAG_LENGTH = 23
            private val ANONYMOUS_CLASS = Pattern.compile("(\\$\\d+)+$")
        }
    }

    companion object {
        private const val APP_VERSION_SAVE_INSTALL_DATE = 27
        private const val APP_VERSION_UPGRADED_CUSTOM_PREFERENCES = 27
    }
}