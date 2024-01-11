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

package io.livekit.android.compose.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import io.livekit.android.compose.local.requireRoom
import io.livekit.android.room.Room
import io.livekit.android.room.participant.Participant
import io.livekit.android.util.flow

/**
 * Remembers the full list of participants, with the local participant included
 * as the first item in the list.
 *
 * Updates automatically whenever the participant list changes.
 */
@Composable
fun rememberParticipants(passedRoom: Room? = null): List<Participant> {
    val room = requireRoom(passedRoom = passedRoom)

    val localParticipant = room.localParticipant
    val remoteParticipants by room::remoteParticipants.flow.collectAsState()

    return remember(localParticipant, remoteParticipants) {
        return@remember listOf(localParticipant).plus(remoteParticipants.values)
    }
}
