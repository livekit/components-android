/*
 * Copyright 2025 LiveKit, Inc.
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

package io.livekit.android.compose.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState

/**
 * A utility state that either collects from the state provided from [block],
 * or emits the [default] value if the state is null.
 *
 * The returned state will always refer to the same [State] object.
 */
@Composable
internal inline fun <T> rememberStateOrDefault(default: T, block: @Composable () -> State<T>?): State<T> {
    val blockState = rememberUpdatedState(block())

    return remember {
        derivedStateOf {
            blockState.value?.value
                ?: default
        }
    }
}
