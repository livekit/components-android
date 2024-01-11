/*
 * Copyright 2023-2024 LiveKit, Inc.
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
import androidx.compose.runtime.remember
import io.livekit.android.compose.local.requireRoom
import io.livekit.android.room.Room
import io.livekit.android.util.flow

/**
 * Holder for basic [Room] information.
 */
data class RoomInfo(
    val name: State<String?>,
    val metadata: State<String?>,
)

/**
 * Remembers the room info and updates whenever it is changed.
 */
@Composable
fun rememberRoomInfo(passedRoom: Room? = null): RoomInfo {
    val room = requireRoom(passedRoom = passedRoom)

    val name = room::name.flow.collectAsState()
    val metadata = room::metadata.flow.collectAsState()

    val roomInfo = remember(room) {
        RoomInfo(
            name = name,
            metadata = metadata,
        )
    }

    return roomInfo
}
