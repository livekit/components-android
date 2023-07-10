package io.livekit.android.sample.livestream.room.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import io.livekit.android.room.participant.Participant
import io.livekit.android.sample.livestream.room.data.ParticipantMetadata

/**
 * Finds all onstage participants.
 */
@Composable
fun rememberOnStageParticipants(hostIdentity: String): List<Participant> {
    val metadatas = rememberParticipantMetadatas()

    return remember(metadatas) {
        derivedStateOf {
            return@derivedStateOf metadatas
                .filter<Participant, ParticipantMetadata> { (participant, metadata) -> metadata.isOnStage && participant.identity != hostIdentity }
                .keys
                .sortedBy<Participant, String> { it.identity ?: "" }
        }
    }.value
}