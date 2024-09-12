package io.livekit.android.compose.state.transcriptions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import io.livekit.android.annotations.Beta
import io.livekit.android.compose.flow.rememberEventSelector
import io.livekit.android.compose.types.TrackReference
import io.livekit.android.events.TrackPublicationEvent
import io.livekit.android.room.Room
import io.livekit.android.room.types.TranscriptionSegment

/**
 * Collect all the transcriptions for the room.
 */
@Beta
@Composable
fun rememberRoomTranscriptions(room: Room) {

}

/**
 * Collect all the transcriptions for a track.
 */
@Beta
@Composable
fun rememberTrackTranscriptions(trackReference: TrackReference) {
    val segments = remember { mutableStateListOf<TranscriptionSegment>() }

    if(trackReference.publication != null) {
        val events = rememberEventSelector<TrackPublicationEvent.TranscriptionReceived>(trackReference.publication)

        LaunchedEffect(events) {
            segments.clear()
            events.collect {

            }
        }
    }
}

data class ReceivedTranscriptionSegment(
    val segment: TranscriptionSegment,
    val timestamp: Long,
)