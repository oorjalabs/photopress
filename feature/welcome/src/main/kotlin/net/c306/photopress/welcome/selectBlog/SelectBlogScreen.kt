package net.c306.photopress.welcome.selectBlog

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import net.c306.photopress.core.designsystem.BasePalette
import net.c306.photopress.core.designsystem.CustomColour
import net.c306.photopress.welcome.R
import net.c306.photopress.welcome.WelcomeTheme

object SelectBlogScreen {
    internal object Style {
        val TitleColour = CustomColour(
            light = BasePalette.GREY_800,
            dark = BasePalette.GREY_50,
        )
        val SubTitleColour = CustomColour(
            light = BasePalette.GREY_800,
            dark = BasePalette.GREY_200,
        )
        val SecondaryButtonTextColour = CustomColour(
            light = BasePalette.secondaryDarkColor,
            dark = BasePalette.secondaryLightColor,
        )
        val PrimaryButtonTextColour = CustomColour(
            light = BasePalette.GREY_50,
            dark = BasePalette.GREY_950,
        )
    }

    sealed interface State {
        data object NoBlogAvailable : State
        data object NoBlogSelected : State
        data class BlogSelected(val displayName: String) : State
    }
}

@Composable
fun SelectBlogScreen(modifier: Modifier = Modifier) {
    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.heightIn(min = 16.dp, max = 128.dp))

            Image(
                painter = painterResource(R.drawable.ic_puppy_full_white),
                contentDescription = stringResource(id = R.string.cd_photopress_logo),
                colorFilter = ColorFilter.tint(color = BasePalette.GREY_50),
                modifier = Modifier.size(128.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(id = R.string.title_welcome_last),
                style = MaterialTheme.typography.headlineLarge,
                color = SelectBlogScreen.Style.TitleColour.current,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(id = R.string.subtitle_welcome_select_blog),
                color = SelectBlogScreen.Style.SubTitleColour.current,
                style = MaterialTheme.typography.bodyLarge,
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@PreviewLightDark
@Composable
private fun Preview() {
    WelcomeTheme {
        SelectBlogScreen()
    }
}