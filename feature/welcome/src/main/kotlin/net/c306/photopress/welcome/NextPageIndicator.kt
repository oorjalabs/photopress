package net.c306.photopress.welcome

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.compose.rememberLottieDynamicProperties
import com.airbnb.lottie.compose.rememberLottieDynamicProperty
import net.c306.photopress.core.designsystem.BasePalette
import net.c306.photopress.core.designsystem.CustomColour

private object NextPageIndicator {
    object Style {
        val Tint = CustomColour(
            light = BasePalette.GREY_700,
            dark = BasePalette.GREY_300,
        )
    }
}

@Composable
internal fun NextPageIndicator(modifier: Modifier = Modifier) {
    val tint = NextPageIndicator.Style.Tint.current

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
                    tint.hashCode(),
                    PorterDuff.Mode.SRC_ATOP
                )
            })
    )
    LottieAnimation(
        composition = composition,
        modifier = modifier.width(48.dp),
        speed = 0.5f,
        isPlaying = true,
        iterations = LottieConstants.IterateForever,
        dynamicProperties = dynamicProperties,
    )
}