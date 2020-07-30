package net.c306.photopress.utils

import android.content.pm.PackageManager

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