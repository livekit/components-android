package io.livekit.android.compose.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import io.livekit.android.compose.local.requireParticipant
import io.livekit.android.room.participant.Participant
import io.livekit.android.util.flow

data class ParticipantInfo(
    val name: String?,
    val identity: String?,
    val metadata: String?,
)

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