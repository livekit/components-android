/*
 * Copyright 2023 LiveKit, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
