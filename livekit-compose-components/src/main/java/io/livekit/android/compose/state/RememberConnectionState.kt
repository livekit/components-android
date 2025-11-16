/*
 * Copyright 2024 LiveKit, Inc.
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

package io.livekit.android.compose.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import io.livekit.android.compose.local.RoomScope
import io.livekit.android.compose.local.requireRoom
import io.livekit.android.room.Room
import io.livekit.android.util.flow

/**
 * Returns the [Room.State] from [passedRoom] or the local [RoomScope] if null.
 */
@Composable
fun rememberConnectionState(passedRoom: Room? = null): State<Room.State> {
    val room = requireRoom(passedRoom)
    return room::state.flow.collectAsState()
}
