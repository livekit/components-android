package io.livekit.android.sample.livestream.room.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import io.livekit.android.compose.local.RoomLocal
import io.livekit.android.sample.livestream.room.data.ParticipantMetadata
import io.livekit.android.sample.livestream.room.data.RoomMetadata
import io.livekit.android.util.flow

/**
 * Parses the room's metadata as [ParticipantMetadata].
 */
@Composable
fun rememberRoomMetadata(): State<RoomMetadata> {
    val room = RoomLocal.current
    val metadataState = room::metadata.flow
        .collectAsState()
    val metadata = remember {
        derivedStateOf {
            metadataState.value
                ?.takeIf { it.isNotBlank() }
                ?.let { metadata ->
                    RoomMetadata.fromJson(metadata)
                }
                ?: RoomMetadata(
                    creatorIdentity = "",
                    enableChat = false,
                    allowParticipation = false
                )
        }
    }

    return metadata
}
