package io.livekit.android.compose.types

import io.livekit.android.room.participant.Participant
import io.livekit.android.room.track.Track
import io.livekit.android.room.track.TrackPublication

interface TrackIdentifier {
    val participant: Participant

    fun getTrackPublication(): TrackPublication?
}

/**
 * Identifies a track based on the source and/or name. At least one is required.
 */
data class TrackSource(
    override val participant: Participant,
    val source: Track.Source? = null,
    val name: String? = null,
) : TrackIdentifier {
    init {
        require(source != null || name != null) { "At least one of source or name must be provided!" }
    }

    override fun getTrackPublication(): TrackPublication? {
        return if (source != null && name != null) {
            participant.tracks.values
                .firstOrNull { p -> p.source == source && p.name == name }
        } else if (source != null) {
            participant.getTrackPublication(source)
        } else if (name != null) {
            participant.getTrackPublicationByName(name)
        } else {
            throw IllegalStateException("At least one of source or name must be provided!")
        }
    }
}

class TrackReference(
    override val participant: Participant,
    val publication: TrackPublication?,
    val source: Track.Source,
) : TrackIdentifier {
    override fun getTrackPublication(): TrackPublication? {
        return publication
    }

    fun isPlaceholder(): Boolean {
        return publication == null
    }
}