package net.c306.photopress

import android.app.Application
import android.os.StrictMode
import timber.log.Timber
import java.util.regex.Pattern

class PhotoPressApplication : Application() {

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

}