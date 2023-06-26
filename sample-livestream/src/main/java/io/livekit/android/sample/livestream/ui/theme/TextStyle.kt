package io.livekit.android.sample.livestream.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object LKTextStyle {

    val largeButton by lazy {
        TextStyle(
            fontSize = 17.sp,
            fontWeight = FontWeight.W700,
        )
    }

    val header by lazy {
        TextStyle(
            fontSize = 20.sp,
            fontWeight = FontWeight.W700,
        )
    }

    val listSectionHeader by lazy {
        TextStyle(
            fontSize = 11.sp,
            fontWeight = FontWeight.W700,
            lineHeight = 20.sp,
            letterSpacing = 1.sp,
            color = Color(0x80FFFFFF)
        )
    }

    val roomButton by lazy {
        TextStyle(
            fontSize = 13.sp,
            fontWeight = FontWeight.W600,
            color = Color(0xFFFFFFFF),
            platformStyle = PlatformTextStyle(
                includeFontPadding = false
            )
        )
    }
}