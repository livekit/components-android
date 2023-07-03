package io.livekit.android.compose.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import io.livekit.android.compose.local.rememberLiveKitRoom
import io.livekit.android.room.Room
import io.livekit.android.util.flow

data class RoomInfo(
    val name: State<String?>,
    val metadata: State<String?>,
)

@Composable
fun rememberRoomInfo(passedRoom: Room? = null): RoomInfo {
    val room = rememberLiveKitRoom(passedRoom = passedRoom)

    val name = room::name.flow.collectAsState()
    val metadata = room::metadata.flow.collectAsState()

    val roomInfo = remember(room) {
        RoomInfo(
            name = name,
            metadata = metadata,
        )
    }

    return roomInfo
}