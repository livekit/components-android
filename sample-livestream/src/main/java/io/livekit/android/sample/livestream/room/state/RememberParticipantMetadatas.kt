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

package io.livekit.android.sample.livestream.room.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import io.livekit.android.compose.state.rememberParticipants
import io.livekit.android.room.participant.Participant
import io.livekit.android.sample.livestream.room.data.ParticipantMetadata
import io.livekit.android.util.flow

/**
 * Parses a participant's metadata as [ParticipantMetadata].
 */
@Composable
fun rememberParticipantMetadata(participant: Participant): ParticipantMetadata {
    val metadataState = participant::metadata.flow
        .collectAsState()
    val participantMetadata = remember {
        derivedStateOf {
            metadataState.value
                ?.takeIf { it.isNotBlank() }
                ?.let { metadata ->
                    ParticipantMetadata.fromJson(metadata)
                }
                ?: ParticipantMetadata(handRaised = false, invitedToStage = false)
        }
    }

    return participantMetadata.value
}

/**
 * Parses the list of participants and maps them with their [ParticipantMetadata].
 */
@Composable
fun rememberParticipantMetadatas(): Map<Participant, ParticipantMetadata> {
    val participants = rememberParticipants()
    return participants.associateWith { rememberParticipantMetadata(it) }
}
