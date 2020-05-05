package net.c306.photopress.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri
import android.os.Build
import net.c306.photopress.R

object Utils {

    internal fun getIntentForUrl(url: String): Intent {
        return Intent(ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(FLAG_ACTIVITY_NEW_TASK)
        }
    }

    internal fun copyToClipboard(context: Context, text: String, label: String? = null) {
        (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
            .setPrimaryClip(
                ClipData.newPlainText(
                    label ?: context.getString(R.string.app_name),
                    text
                )
            )
    }


    internal fun sendSharingIntent(context: Context, text: String, title: String) {
        /**
         * Why? Why, to give you a taste of your future, a preview of things to come.
         * Con permiso, Capitan. The hall is rented, the orchestra engaged.
         * It's now time to see if you can dance.
         */
        val isQorLater = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            if (isQorLater) {
                putExtra(Intent.EXTRA_TITLE, title)
            }
            type = MIME_TYPE_TEXT
        }

        context.startActivity(
            Intent.createChooser(sendIntent, if (isQorLater) "" else title).apply {
                addFlags(FLAG_ACTIVITY_NEW_TASK)
            }
        )
    }


    private const val MIME_TYPE_TEXT = "text/plain"

}
