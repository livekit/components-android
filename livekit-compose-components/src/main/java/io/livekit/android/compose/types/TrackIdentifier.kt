/*
 * Copyright 2023-2025 LiveKit, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.livekit.android.compose.types

import androidx.compose.runtime.Immutable
import io.livekit.android.room.participant.Participant
import io.livekit.android.room.track.Track
import io.livekit.android.room.track.TrackPublication

/**
 * Identifying information for a track.
 */
interface TrackIdentifier {
    val participant: Participant

    val publication: TrackPublication?
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

    override val publication: TrackPublication?
        get() {
            return if (source != null && name != null) {
                participant.trackPublications.values
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

/**
 * A reference to a [Track], or a placeholder.
 */
@Immutable
data class TrackReference(
    override val participant: Participant,
    override val publication: TrackPublication?,
    val source: Track.Source,
) : TrackIdentifier {

    fun isPlaceholder(): Boolean {
        return publication == null
    }

    fun isSubscribed(): Boolean {
        return !isPlaceholder() && (publication?.subscribed ?: false)
    }
}
