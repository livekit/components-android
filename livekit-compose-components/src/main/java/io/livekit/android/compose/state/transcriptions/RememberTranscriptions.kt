package io.livekit.android.compose.state.transcriptions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import io.livekit.android.annotations.Beta
import io.livekit.android.compose.flow.rememberEventSelector
import io.livekit.android.compose.local.requireParticipant
import io.livekit.android.compose.local.requireRoom
import io.livekit.android.compose.types.TrackReference
import io.livekit.android.events.ParticipantEvent
import io.livekit.android.events.RoomEvent
import io.livekit.android.events.TrackPublicationEvent
import io.livekit.android.room.Room
import io.livekit.android.room.participant.Participant
import io.livekit.android.room.types.TranscriptionSegment
import io.livekit.android.room.types.mergeNewSegments
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Collect all the transcriptions for the room.
 */
@Beta
@Composable
fun rememberTranscriptions(passedRoom: Room? = null): List<TranscriptionSegment> {
    val room = requireRoom(passedRoom)
    val events = rememberEventSelector<RoomEvent.TranscriptionReceived>(room)
        .map { it.transcriptionSegments }

    return rememberTranscriptionsImpl(transcriptionsFlow = events)
}

/**
 * Collect all the transcriptions for a track reference.
 */
@Beta
@Composable
fun rememberTranscriptions(trackReference: TrackReference): List<TranscriptionSegment> {
    val publication = trackReference.publication ?: return emptyList()
    val events = rememberEventSelector<TrackPublicationEvent.TranscriptionReceived>(publication)
        .map { it.transcriptions }

    return rememberTranscriptionsImpl(transcriptionsFlow = events)
}

/**
 * Collect all the transcriptions for a participant.
 */
@Beta
@Composable
fun rememberTranscriptions(passedParticipant: Participant? = null): List<TranscriptionSegment> {
    val participant = requireParticipant(passedParticipant)
    val events = rememberEventSelector<ParticipantEvent.TranscriptionReceived>(participant)
        .map { it.transcriptions }

    return rememberTranscriptionsImpl(transcriptionsFlow = events)
}


@Composable
internal fun rememberTranscriptionsImpl(transcriptionsFlow: Flow<List<TranscriptionSegment>>): List<TranscriptionSegment> {
    val segments = remember { mutableStateMapOf<String, TranscriptionSegment>() }
    val orderedSegments = remember {
        derivedStateOf {
            segments.values.sortedBy { segment -> segment.firstReceivedTime }
        }
    }
    LaunchedEffect(transcriptionsFlow) {
        segments.clear()
        transcriptionsFlow.collect {
            segments.mergeNewSegments(it)
        }
    }

    return orderedSegments.value
}