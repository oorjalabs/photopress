package net.c306.photopress.utils

import android.content.Context
import android.content.pm.PackageManager
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.annotation.StringRes

fun PackageManager.isPackageInstalled(
    packageName: String
): Boolean {
    return try {
        getPackageInfo(packageName, 0)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
}


/**
 * Set alternate hint for TextInputEditText on focus, and force show keyboard
 */
internal fun EditText.setInputFocus(hasFocus: Boolean, @Suppress("SameParameterValue") @StringRes stringId: Int) {
    
    if (!hasFocus) {
        hint = null
        return
    }
    
    // Set hint for edit text only on focus. In non focus mode, the hint for edit text layout is shown
    hint = context.getString(stringId)
    
    // Due to capturing first focus tab here, the keyboard isn't shown. So, force it to show
    showKeyboard()
}

internal fun View.hideKeyboard() {
    // Due to capturing first focus tab here, the keyboard isn't shown. So, force it to show
    (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?)
        ?.hideSoftInputFromWindow(windowToken, 0)
}

internal fun View.showKeyboard() {
    // Due to capturing first focus tab here, the keyboard isn't shown. So, force it to show
    (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?)
        ?.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}