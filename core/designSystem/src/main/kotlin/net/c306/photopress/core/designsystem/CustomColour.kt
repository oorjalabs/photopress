package net.c306.photopress.core.designsystem

import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Encapsulates light and dark mode colours.
 * If [dark] colour isn't provided, defaults to [light] in dark mode.
 *
 * Access the correct colours for current app state using the [current] property.
 *
 * **Dev note**: using `isSystemInDarkMode()` is an expensive operation, so we should do it once at top
 * level and pass the resulting theme as composition local. This will also let us handle the
 * scenarios where a user has selected always light/dark theme.
 */
open class CustomColour(
    @get:VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val light: Color,
    @get:VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val dark: Color = light,
) {
    /**
     * Provides the card colour depending on whether dark mode is active or not
     *
     * If the composition tree is not wrapped in [AppColourMode], then the value returned would
     * always be for the light mode.
     */
    val current: Color
        @Composable
        get() = if (LocalIsDarkModeActive.current) dark else light

    object Unspecified : CustomColour(Color.Unspecified)
}