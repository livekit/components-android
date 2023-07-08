package io.livekit.android.sample.livestream.room.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import io.livekit.android.compose.state.rememberParticipantInfo
import io.livekit.android.compose.state.rememberParticipants
import io.livekit.android.room.participant.Participant
import io.livekit.android.sample.livestream.room.data.RoomMetadata

/**
 * Finds the creator of the room.
 */
@Composable
fun rememberHostParticipant(roomMetadata: RoomMetadata): Participant? {
    val participantInfos = rememberParticipants().associateWith { rememberParticipantInfo(it) }

    val hostParticipant = remember(participantInfos) {
        derivedStateOf {
            participantInfos.firstNotNullOfOrNull { (participant, info) ->
                participant.takeIf {
                    info.identity == roomMetadata.creatorIdentity
                }
            }
        }
    }
    return hostParticipant.value
}