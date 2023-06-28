package io.livekit.android.sample.livestream.room.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.github.ajalt.timberkt.Timber
import io.livekit.android.compose.local.RoomLocal
import io.livekit.android.compose.local.rememberLiveKitRoom
import io.livekit.android.events.RoomEvent
import io.livekit.android.events.collect
import io.livekit.android.room.Room
import io.livekit.android.room.participant.Participant
import io.livekit.android.sample.livestream.room.data.ParticipantMetadata
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

private fun findHost(room: Room): Participant? {
    var host: Participant? = null
    val participants = listOf(room.localParticipant).plus(room.remoteParticipants.values)

    host = participants.firstOrNull { p ->
        val metadata = p.metadata ?: return@firstOrNull false
        Timber.e { "$p: $metadata" }
        val participantMetadata = Json.decodeFromString<ParticipantMetadata>(metadata)
        return@firstOrNull participantMetadata.isCreator
    }
    return host
}

@Composable
fun rememberHostParticipant(): State<Participant?> {
    val room by rememberLiveKitRoom(passedRoom = RoomLocal.current)

    val hostParticipantState = remember {
        mutableStateOf(findVideoHost(room))
    }

    LaunchedEffect(room) {
        room.events.collect {
            when (it) {
                is RoomEvent.ParticipantMetadataChanged -> {
                    Timber.e { it.toString() }
                    hostParticipantState.value = findHost(room)
                }

                else -> {
                    /* do nothing */
                    Timber.e { it.toString() }
                }
            }
        }
    }

    return hostParticipantState
}