package io.livekit.android.compose.sorting

import io.livekit.android.compose.types.TrackReference
import io.livekit.android.room.participant.LocalParticipant
import io.livekit.android.room.track.Track

/**
 * Default sort for a list of [TrackReference]. Orders by:
 *
 * 1. local camera track (publication.isLocal)
 * 2. remote screen_share track
 * 3. local screen_share track
 * 4. remote dominant speaker camera track (sorted by speaker with the loudest audio level)
 * 5. other remote speakers that are recently active
 * 6. remote unmuted camera tracks
 * 7. remote tracks sorted by joinedAt
 */
fun sortTrackReferences(trackRefs: List<TrackReference>): List<TrackReference> {

    val localTracks = mutableListOf<TrackReference>()
    val screenShareTracks = mutableListOf<TrackReference>()
    val cameraTracks = mutableListOf<TrackReference>()
    val undefinedTracks = mutableListOf<TrackReference>()

    trackRefs.forEach { trackRef ->
        if (trackRef.participant is LocalParticipant && trackRef.source == Track.Source.CAMERA) {
            localTracks.add(trackRef)
        } else if (trackRef.source == Track.Source.SCREEN_SHARE) {
            screenShareTracks.add(trackRef)
        } else if (trackRef.source == Track.Source.CAMERA) {
            cameraTracks.add(trackRef)
        } else {
            undefinedTracks.add(trackRef)
        }
    }

    val sortedScreenShareTracks = sortScreenShareTracks(screenShareTracks)
    val sortedCameraTracks = sortCameraTracks(cameraTracks)

    return localTracks
        .plus(sortedScreenShareTracks)
        .plus(sortedCameraTracks)
        .plus(undefinedTracks)
}

/**
 * Sort an array of screen share [TrackReference].
 * Main sorting order:
 * 1. remote screen shares
 * 2. local screen shares
 * Secondary sorting by participant's joining time.
 */
private fun sortScreenShareTracks(screenShareTracks: List<TrackReference>): List<TrackReference> {
    val localScreenShares = screenShareTracks.filter { it.participant is LocalParticipant }
    val remoteScreenShares = screenShareTracks.filter { it.participant !is LocalParticipant }
        .sortedBy { it.participant.joinedAt }

    return localScreenShares.plus(remoteScreenShares)
}

/**
 * Sort an array of camera [TrackReference].
 */
private fun sortCameraTracks(cameraTracks: List<TrackReference>): List<TrackReference> {

    return cameraTracks.sortedWith { a, b ->
        // Participant with higher audio level goes first.
        if (a.participant.isSpeaking && b.participant.isSpeaking) {
            return@sortedWith compareAudioLevel(a.participant, b.participant);
        }

        // A speaking participant goes before one that is not speaking.
        if (a.participant.isSpeaking != b.participant.isSpeaking) {
            return@sortedWith compareIsSpeaking(a.participant, b.participant);
        }

        // A participant that spoke recently goes before a participant that spoke a while back.
        if (a.participant.lastSpokeAt != b.participant.lastSpokeAt) {
            return@sortedWith compareLastSpokenAt(a.participant, b.participant);
        }

        // TrackReference before TrackReferencePlaceholder
        if (a.isPlaceholder() != b.isPlaceholder()) {
            return@sortedWith compareTrackReferencesByPlaceHolder(a, b);
        }

        // Tiles with video on before tiles with muted video track.
        if (a.isEnabled() != b.isEnabled()) {
            return@sortedWith compareTrackReferencesByIsEnabled(a, b);
        }

        // A participant that joined a long time ago goes before one that joined recently.
        return@sortedWith compareJoinedAt(a.participant, b.participant);
    }
}

private fun TrackReference.isEnabled() = (publication?.subscribed ?: false) && !(publication?.muted ?: true)

fun compareTrackReferencesByPlaceHolder(a: TrackReference, b: TrackReference): Int {
    return compareValues(a.isPlaceholder(), b.isPlaceholder())
}

fun compareTrackReferencesByIsEnabled(a: TrackReference, b: TrackReference): Int {
    return compareValues(b.isEnabled(), a.isEnabled())
}