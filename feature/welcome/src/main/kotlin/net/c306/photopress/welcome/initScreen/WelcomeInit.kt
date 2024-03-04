package net.c306.photopress.welcome.initScreen

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterEnd
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.compose.rememberLottieDynamicProperties
import com.airbnb.lottie.compose.rememberLottieDynamicProperty
import net.c306.photopress.core.designsystem.BasePalette
import net.c306.photopress.core.designsystem.CustomColour
import net.c306.photopress.welcome.R
import net.c306.photopress.welcome.WelcomeTheme

private object WelcomeInit {
    object Style {
        val TitleColour = CustomColour(
            light = BasePalette.GREY_800,
            dark = BasePalette.GREY_50,
        )
        val SubTitleColour = CustomColour(
            light = BasePalette.GREY_800,
            dark = BasePalette.GREY_200,
        )
        val StepsColour = CustomColour(
            light = BasePalette.GREY_700,
            dark = BasePalette.GREY_300,
        )
    }
}

@Composable
internal fun WelcomeInit(modifier: Modifier = Modifier) {
    Surface(modifier = modifier) {
        Column(
            modifier = Modifier.padding(
                horizontal = 16.dp
            ).fillMaxSize(),
            horizontalAlignment = CenterHorizontally,
        ) {
            Spacer(
                modifier = Modifier
                    .weight(1f, true)
                    .heightIn(min = 16.dp, max = 128.dp)
            )
            Image(
                painter = painterResource(R.drawable.ic_puppy_full_white),
                contentDescription = stringResource(id = R.string.cd_photopress_logo),
                colorFilter = ColorFilter.tint(color = BasePalette.GREY_50),
                modifier = Modifier.size(128.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(id = R.string.title_welcome_to_photopress),
                style = MaterialTheme.typography.headlineLarge,
                color = WelcomeInit.Style.TitleColour.current,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(id = R.string.subtitle_welcome_to_photopress),
                color = WelcomeInit.Style.SubTitleColour.current,
                style = MaterialTheme.typography.bodyLarge,
            )


            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier.fillMaxWidth()) {
                val stepsContentColour = WelcomeInit.Style.StepsColour.current
                Text(
                    text = stringResource(id = R.string.message_welcome_to_photopress),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = stepsContentColour,
                        lineHeight = 32.sp,
                        lineHeightStyle = LineHeightStyle(
                            trim = LineHeightStyle.Trim.None,
                            alignment = LineHeightStyle.Alignment.Proportional,
                        )
                    ),
                    modifier = Modifier.align(Center),
                )

                val composition by rememberLottieComposition(
                    LottieCompositionSpec.RawRes(
                        resId = R.raw.anim_swipe_right_arrows
                    )
                )
                val dynamicProperties = rememberLottieDynamicProperties(
                    rememberLottieDynamicProperty(
                        keyPath = arrayOf("**"),
                        property = LottieProperty.COLOR_FILTER,
                        callback = {
                            PorterDuffColorFilter(
                                stepsContentColour.hashCode(),
                                PorterDuff.Mode.SRC_ATOP
                            )
                        })
                )
                LottieAnimation(
                    composition = composition,
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                x = 16.dp.roundToPx(),
                                y = 0,
                            )
                        }
                        .align(CenterEnd)
                        .width(48.dp),
                    speed = 0.5f,
                    isPlaying = true,
                    iterations = LottieConstants.IterateForever,
                    dynamicProperties = dynamicProperties,
                )
            }

            Spacer(modifier = Modifier.height(64.dp))
        }
    }
}

@PreviewLightDark
@Composable
private fun Preview() {
    WelcomeTheme {
        WelcomeInit()
    }
}