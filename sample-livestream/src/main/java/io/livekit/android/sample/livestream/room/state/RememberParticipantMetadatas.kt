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