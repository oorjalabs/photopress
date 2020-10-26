package net.c306.photopress.utils

import android.content.pm.PackageManager
import android.widget.EditText
import androidx.annotation.StringRes
import net.c306.customcomponents.preference.SearchableListPreference
import net.c306.customcomponents.utils.showKeyboard

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

fun SearchableListPreference.setCustomDefaultValue(value: String) {
    val previousValue = values
    
    // Set default value
    if (previousValue.isNullOrEmpty()) {
        values = setOf(value)
    }
}