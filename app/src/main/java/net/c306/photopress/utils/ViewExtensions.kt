package net.c306.photopress.utils

import android.content.Context
import android.content.pm.PackageManager
import android.util.TypedValue

fun Context.getFloatFromXml(id: Int): Float {
    TypedValue().let {
        resources.getValue(id, it, true)
        return it.float
    }
}


fun Context.getAndroidAttributeId(id: Int): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(id, typedValue, true)
    return typedValue.resourceId
}


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