package net.c306.photopress.welcome

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import net.c306.photopress.core.designsystem.AppColourMode
import net.c306.photopress.core.designsystem.BasePalette
import net.c306.photopress.core.designsystem.LocalIsDarkModeActive

private val welcomeLightColourScheme = lightColorScheme(
    surface = BasePalette.primaryLightColor,
    primary = BasePalette.p3,
)

private val welcomeDarkColourScheme = darkColorScheme(
    surface = BasePalette.primaryDarkColor,
    primary = BasePalette.p1_night,
)

/**
 * A wrapper around [MaterialTheme] that provides the [LocalIsDarkModeActive] and the appropriate
 * [MaterialTheme.colorScheme] based on the system's dark mode status.
 */
@Composable
fun WelcomeTheme(
    content: @Composable () -> Unit,
) {
    AppColourMode {
        MaterialTheme(
            colorScheme = if (LocalIsDarkModeActive.current) {
                welcomeDarkColourScheme
            } else {
                welcomeLightColourScheme
            },
        ) {
            content()
        }
    }
}