package io.livekit.android.compose.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import io.livekit.android.compose.local.requireRoom
import io.livekit.android.room.Room
import io.livekit.android.room.participant.Participant
import io.livekit.android.util.flow

@Composable
fun rememberParticipants(passedRoom: Room? = null): List<Participant> {
    val room = requireRoom(passedRoom = passedRoom)

    val localParticipant = room.localParticipant
    val remoteParticipants by room::remoteParticipants.flow.collectAsState()

    return remember(localParticipant, remoteParticipants) {
        return@remember listOf(localParticipant).plus(remoteParticipants.values)
    }
}