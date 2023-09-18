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

import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object LKButtonColors {
    @Composable
    fun blueButtonColors(): ButtonColors {
        return ButtonDefaults.buttonColors(
            containerColor = Blue500,
            contentColor = Color.White
        )
    }

    @Composable
    fun secondaryButtonColors(): ButtonColors {
        return ButtonDefaults.buttonColors(
            contentColor = Color.White,
            containerColor = Color(0xFF131313)
        )
    }
}
