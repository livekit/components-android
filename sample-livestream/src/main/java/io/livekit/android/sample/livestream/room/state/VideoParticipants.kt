package io.livekit.android.sample.livestream.room.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.livekit.android.room.participant.Participant

/**
 * Finds all onstage participants.
 */
@Composable
fun rememberOnStageParticipants(): Set<Participant> {
    val metadatas = rememberParticipantMetadatas()

    return remember(metadatas) {
        metadatas
            .filter { (_, metadata) -> metadata.isOnStage }
            .keys
    }
}