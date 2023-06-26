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
import io.livekit.android.events.ParticipantEvent
import io.livekit.android.events.RoomEvent
import io.livekit.android.events.collect
import io.livekit.android.room.Room
import io.livekit.android.room.participant.Participant
import io.livekit.android.util.flow

fun findVideoHost(room: Room): Participant? {
    var host: Participant? = null
    if (room.localParticipant.videoTracks.isNotEmpty()) {
        host = room.localParticipant
    } else {
        for (participantEntry in room.remoteParticipants) {
            val (_, participant) = participantEntry

            if (participant.videoTracks.isNotEmpty()) {
                host = participant
            }
        }
    }

    Timber.e { "found host $host" }
    return host
}

@Composable
fun rememberVideoHostParticipant(): State<Participant?> {
    val room by rememberLiveKitRoom(passedRoom = RoomLocal.current)

    val hostParticipantState = remember {
        mutableStateOf(findVideoHost(room))
    }

    LaunchedEffect(room) {
        room.events.collect {
            when (it) {
                is RoomEvent.TrackSubscribed,
                is RoomEvent.TrackUnsubscribed -> {
                    Timber.e { it.toString() }
                    hostParticipantState.value = findVideoHost(room)
                }

                else -> {
                    /* do nothing */
                    Timber.e { it.toString() }
                }
            }
        }
    }

    LaunchedEffect(room.localParticipant) {
        Timber.e { "started watching video tracks" }
        room.localParticipant::videoTracks.flow.collect {
            Timber.e { "videotracks: $it" }
        }
    }

    LaunchedEffect(room.localParticipant) {
        Timber.e { "started watching all tracks" }
        room.localParticipant::tracks.flow.collect {
            Timber.e { "all tracks: $it" }
        }
    }
    LaunchedEffect(room) {
        room.localParticipant.events.collect {
            when (it) {
                is ParticipantEvent.LocalTrackPublished,
                is ParticipantEvent.LocalTrackUnpublished -> {
                    Timber.e { it.toString() }
                    hostParticipantState.value = findVideoHost(room)
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