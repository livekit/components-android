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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import io.livekit.android.compose.local.requireParticipant
import io.livekit.android.room.participant.Participant
import io.livekit.android.util.flow

/**
 * Holder for basic [Participant] information.
 */
data class ParticipantInfo(
    val name: String?,
    val identity: Participant.Identity?,
    val metadata: String?,
)

/**
 * Remembers the participant info and updates whenever it is changed.
 */
@Composable
fun rememberParticipantInfo(passedParticipant: Participant? = null): ParticipantInfo {
    val participant = requireParticipant(passedParticipant)

    val name = participant::name.flow.collectAsState().value
    val identity = participant::identity.flow.collectAsState().value
    val metadata = participant::metadata.flow.collectAsState().value

    val participantInfo = remember(name, identity, metadata) {
        ParticipantInfo(
            name = name,
            identity = identity,
            metadata = metadata,
        )
    }

    return participantInfo
}
