package io.livekit.android.sample.livestream.room.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import io.livekit.android.compose.state.rememberParticipantInfo
import io.livekit.android.compose.state.rememberParticipants
import io.livekit.android.room.participant.Participant

/**
 * Finds the creator of the room.
 */
@Composable
fun rememberHostParticipant(identity: String): Participant? {
    val participantInfos = rememberParticipants().associateWith { rememberParticipantInfo(it) }

    val hostParticipant = remember(participantInfos) {
        derivedStateOf {
            participantInfos.firstNotNullOfOrNull { (participant, info) ->
                participant.takeIf {
                    info.identity == identity
                }
            }
        }
    }
    return hostParticipant.value
}