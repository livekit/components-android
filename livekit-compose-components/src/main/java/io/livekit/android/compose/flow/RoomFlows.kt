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

package io.livekit.android.compose.flow

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import io.livekit.android.compose.local.RoomScope
import io.livekit.android.events.RoomEvent
import io.livekit.android.events.collect
import io.livekit.android.room.Room
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * A utility method to obtain a flow for specific room events.
 *
 * Pass in `RoomEvent` as the type to receive all room events.
 *
 */
@Composable
inline fun <reified T : RoomEvent> rememberEventSelector(room: Room): State<Flow<T>> {
    val flow = remember(room) {
        mutableStateOf(MutableSharedFlow<T>(extraBufferCapacity = 100))
    }

    LaunchedEffect(room) {
        room.events.collect {
            if (it is T) {
                flow.value.emit(it)
            }
        }
    }

    return flow
}
