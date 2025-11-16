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
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import io.livekit.android.compose.local.requireParticipant
import io.livekit.android.room.participant.Participant
import io.livekit.android.util.flow

/**
 * Holder for basic [Participant] information.
 */
@Stable
class ParticipantInfo(
    nameState: State<String?>,
    identityState: State<Participant.Identity?>,
    metadataState: State<String?>,
) {
    val name: String? by nameState
    val identity: Participant.Identity? by identityState
    val metadata: String? by metadataState
}

/**
 * Remembers the participant info and updates whenever it is changed.
 */
@Composable
fun rememberParticipantInfo(passedParticipant: Participant? = null): ParticipantInfo {
    val participant = requireParticipant(passedParticipant)

    val nameState = participant::name.flow.collectAsState()
    val identityState = participant::identity.flow.collectAsState()
    val metadataState = participant::metadata.flow.collectAsState()

    val participantInfo = remember(nameState, identityState, metadataState) {
        ParticipantInfo(
            nameState = nameState,
            identityState = identityState,
            metadataState = metadataState,
        )
    }

    return participantInfo
}
