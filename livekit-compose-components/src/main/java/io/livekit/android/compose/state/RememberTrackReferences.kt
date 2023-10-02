package io.livekit.android.compose.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import io.livekit.android.compose.local.RoomLocal
import io.livekit.android.compose.local.requireRoom
import io.livekit.android.compose.types.TrackReference
import io.livekit.android.events.RoomEvent
import io.livekit.android.room.Room
import io.livekit.android.room.track.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

/**
 * Returns an array of TrackReferences depending the sources provided.
 *
 * @param sources The sources of the tracks to provide. Defaults to all tracks.
 * @param usePlaceholders A set of sources to provide placeholders for.
 *     A placeholder will provide a TrackReference for participants that don't
 *     yet have a track published for that source. Defaults to no placeholders.
 * @param passedRoom The room to use on, or [RoomLocal] if null.
 * @param updateOn Room events to listen to. Defaults to all events.
 * @param onlySubscribed If true, only return tracks that have been subscribed. Defaults to true.
 */
@Composable
fun rememberTrackReferences(
    sources: List<Track.Source> = listOf(
        Track.Source.CAMERA,
        Track.Source.MICROPHONE,
        Track.Source.SCREEN_SHARE,
        Track.Source.UNKNOWN
    ),
    usePlaceholders: Set<Track.Source> = emptySet(),
    passedRoom: Room? = null,
    updateOn: Set<Class<RoomEvent>>? = null,
    onlySubscribed: Boolean = true,
): State<List<TrackReference>> {
    val room = requireRoom(passedRoom)

    return trackReferencesFlow(
        room = room,
        sources = sources,
        usePlaceholders = usePlaceholders,
        updateOn = updateOn,
        onlySubscribed = onlySubscribed
    ).collectAsState(initial = room.getTrackReferences(sources, usePlaceholders, onlySubscribed))
}

fun trackReferencesFlow(
    room: Room,
    sources: List<Track.Source>,
    usePlaceholders: Set<Track.Source> = emptySet(),
    updateOn: Set<Class<RoomEvent>>? = null,
    onlySubscribed: Boolean = true,
): Flow<List<TrackReference>> {
    return room.events.events
        .filter { updateOn == null || updateOn.contains(it::class.java) }
        .map { room.getTrackReferences(sources, usePlaceholders, onlySubscribed) }
}

fun Room.getTrackReferences(
    sources: List<Track.Source>,
    usePlaceholders: Set<Track.Source> = emptySet(),
    onlySubscribed: Boolean = true
): List<TrackReference> {
    val allParticipants = listOf(localParticipant).plus(remoteParticipants.values)
    return allParticipants.flatMap { participant ->
        sources.map { source ->
            var tracks = participant.tracks.values.mapNotNull { trackPub ->
                if (trackPub.source == source &&
                    (!onlySubscribed || trackPub.subscribed)
                ) {
                    TrackReference(
                        participant = participant,
                        publication = trackPub,
                        source = trackPub.source
                    )
                } else {
                    null
                }
            }
            if (tracks.isEmpty() && usePlaceholders.contains(source)) {
                // Add placeholder
                tracks = listOf(
                    TrackReference(
                        participant = participant,
                        publication = null,
                        source = source,
                    )
                )
            }
            return@flatMap tracks
        }
    }
}