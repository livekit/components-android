package io.livekit.android.sample.livestream.room.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import io.livekit.android.compose.state.rememberParticipants
import io.livekit.android.room.participant.Participant
import io.livekit.android.sample.livestream.room.data.ParticipantMetadata
import io.livekit.android.util.flow

@Composable
fun rememberParticipantMetadatas(): Map<Participant, ParticipantMetadata> {
    val participants = rememberParticipants()
    return participants.associateWith { participant ->
        participant::metadata.flow
            .collectAsState()
            .value
            ?.takeIf { it.isNotBlank() }
            ?.let { metadata ->
                ParticipantMetadata.fromJson(metadata)
            }
            ?: ParticipantMetadata(handRaised = false, invitedToStage = false)
    }
}