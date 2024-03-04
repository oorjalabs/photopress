package net.c306.photopress.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf

/**
 * Default value for is dark mode active when a composable is not wrapped in local [AppColourMode]
 */
val LocalIsDarkModeActive = compositionLocalOf { false }

/**
 * A wrapper that sets the [LocalIsDarkModeActive] so we can correctly use [CustomColour].
 */
@Composable
fun AppColourMode(
    content: @Composable () -> Unit,
) {
    val isDarkModeActive = isSystemInDarkTheme()
    CompositionLocalProvider(LocalIsDarkModeActive provides isDarkModeActive) {
        content()
    }
}