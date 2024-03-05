package net.c306.photopress.welcome.login

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import net.c306.photopress.core.designsystem.BasePalette
import net.c306.photopress.core.designsystem.CustomColour
import net.c306.photopress.welcome.NextPageIndicator
import net.c306.photopress.welcome.R
import net.c306.photopress.welcome.WelcomeTheme

object WelcomeLogin {
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
        data object LoggedOut : State
        data class LoggedIn(val userName: String) : State
    }
}


@Composable
internal fun WelcomeLogin(
    state: WelcomeLogin.State,
    onNextClicked: () -> Unit,
    modifier: Modifier = Modifier,
    onLoginClicked: () -> Unit,
) {
    Surface(modifier = modifier) {
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
                text = stringResource(id = R.string.title_login_to_wordpress),
                style = MaterialTheme.typography.headlineLarge,
                color = WelcomeLogin.Style.TitleColour.current,
            )

            Spacer(modifier = Modifier.height(8.dp))

            val subtitle = when (state) {
                is WelcomeLogin.State.LoggedOut -> {
                    stringResource(id = R.string.subtitle_login_to_wordpress)
                }

                is WelcomeLogin.State.LoggedIn -> {
                    stringResource(id = R.string.connected_as, state.userName)
                }
            }
            Text(
                text = subtitle,
                color = WelcomeLogin.Style.SubTitleColour.current,
                style = MaterialTheme.typography.bodyLarge,
            )

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedContent(targetState = state) {
                when (it) {
                    is WelcomeLogin.State.LoggedOut -> {
                        LoggedOutContent(onClick = onLoginClicked)
                    }

                    is WelcomeLogin.State.LoggedIn -> {
                        LoggedInContent(onNextClicked = onNextClicked)
                    }
                }
            }
        }
    }
}

@Composable
private fun LoggedInContent(
    modifier: Modifier = Modifier,
    onNextClicked: () -> Unit,
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(
                modifier = Modifier
                    .heightIn(min = 16.dp)
                    .weight(1f)
            )

            val composition by rememberLottieComposition(
                LottieCompositionSpec.RawRes(resId = R.raw.anim_done)
            )
            LottieAnimation(
                composition = composition,
                modifier = modifier.size(128.dp),
                isPlaying = true,
            )

            Spacer(
                modifier = Modifier
                    .heightIn(min = 16.dp)
                    .weight(1f)
            )

            OutlinedButton(onClick = onNextClicked) {
                Text(
                    text = stringResource(id = R.string.label_next_pick_a_blog).uppercase(),
                    color = WelcomeLogin.Style.SecondaryButtonTextColour.current,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Spacer(modifier = Modifier.height(72.dp))
        }

        NextPageIndicator(
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = 16.dp.roundToPx(),
                        y = 0,
                    )
                }
                .align(Alignment.CenterEnd)
        )
    }
}

@Composable
private fun LoggedOutContent(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(
            modifier = Modifier
                .heightIn(min = 16.dp)
                .weight(1f)
        )
        Button(
            modifier = modifier,
            onClick = onClick,
            shape = MaterialTheme.shapes.medium,
        ) {
            Text(
                text = stringResource(id = R.string.button_connect_with_wordpress)
                    .uppercase(),
                color = WelcomeLogin.Style.PrimaryButtonTextColour.current,
            )
        }
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Text(
                text = stringResource(id = R.string.message_login_to_wordpress),
                textAlign = TextAlign.Center,
            )
        }
        Spacer(modifier = Modifier.height(48.dp))
    }
}

@PreviewLightDark
@Composable
private fun LoggedOutPreview() {
    WelcomeTheme {
        WelcomeLogin(
            state = WelcomeLogin.State.LoggedOut,
            onNextClicked = {},
        ) {}
    }
}

@PreviewLightDark
@Composable
private fun LoggedInPreview() {
    WelcomeTheme {
        WelcomeLogin(
            state = WelcomeLogin.State.LoggedIn("Adi"),
            onNextClicked = {},
        ) {}
    }
}