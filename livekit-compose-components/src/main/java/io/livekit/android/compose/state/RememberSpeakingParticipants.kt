package io.livekit.android.compose.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import io.livekit.android.compose.local.requireRoom
import io.livekit.android.room.Room
import io.livekit.android.room.participant.Participant
import io.livekit.android.util.flow

@Composable
fun rememberSpeakingParticipants(room: Room? = null): State<List<Participant>> {
    val room = requireRoom(room)

    return room::activeSpeakers.flow.collectAsState()
}