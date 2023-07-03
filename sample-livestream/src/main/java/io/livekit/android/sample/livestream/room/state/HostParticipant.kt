package io.livekit.android.sample.livestream.room.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import com.github.ajalt.timberkt.Timber
import io.livekit.android.compose.state.rememberParticipantInfo
import io.livekit.android.compose.state.rememberParticipants
import io.livekit.android.room.participant.Participant
import io.livekit.android.sample.livestream.room.data.RoomMetadata


@Composable
fun rememberHostParticipant(roomMetadata: RoomMetadata): Participant? {
    val participantInfos = rememberParticipants().associateWith { rememberParticipantInfo(it) }

    val hostParticipant = remember(participantInfos) {
        derivedStateOf {
            participantInfos.firstNotNullOfOrNull { (participant, info) ->
                participant.takeIf {
                    Timber.e { "${info.identity} == ${roomMetadata.creatorIdentity}: ${info.identity == roomMetadata.creatorIdentity}" }
                    info.identity == roomMetadata.creatorIdentity
                }
            }
        }
    }
    return hostParticipant.value
}