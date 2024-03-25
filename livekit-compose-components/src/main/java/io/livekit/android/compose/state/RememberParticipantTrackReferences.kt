/*
 * Copyright 2024 LiveKit, Inc.
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

package io.livekit.android.compose.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import io.livekit.android.compose.local.requireParticipant
import io.livekit.android.compose.local.requireRoom
import io.livekit.android.compose.types.TrackReference
import io.livekit.android.room.Room
import io.livekit.android.room.participant.Participant
import io.livekit.android.room.track.Track
import io.livekit.android.util.flow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest

@Composable
fun rememberParticipantTrackReferences(
    sources: List<Track.Source>,
    participantIdentity: Participant.Identity,
    passedRoom: Room? = null,
    usePlaceholders: Set<Track.Source> = emptySet(),
    onlySubscribed: Boolean = true,
): List<TrackReference> {
    val room = requireRoom(passedRoom)
    val participant = room.getParticipantByIdentity(participantIdentity)

    return rememberParticipantTrackReferences(
        sources = sources,
        passedParticipant = participant,
        usePlaceholders = usePlaceholders,
        onlySubscribed = onlySubscribed
    )
}

@Composable
fun rememberParticipantTrackReferences(
    sources: List<Track.Source> = listOf(
        Track.Source.CAMERA,
        Track.Source.SCREEN_SHARE
    ),
    passedParticipant: Participant? = null,
    usePlaceholders: Set<Track.Source> = emptySet(),
    onlySubscribed: Boolean = true,
): List<TrackReference> {
    val participant = requireParticipant(passedParticipant)

    return participantTrackReferencesFlow(
        participant = participant,
        sources = sources,
        usePlaceholders = usePlaceholders,
        onlySubscribed = onlySubscribed
    )
        .collectAsState(initial = participant.getTrackReferencesBySource(sources, usePlaceholders, onlySubscribed))
        .value
}

/**
 * A flow of the TrackReferences/placeholders in the room.
 *
 * @see rememberTracks
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun participantTrackReferencesFlow(
    participant: Participant,
    sources: List<Track.Source>,
    usePlaceholders: Set<Track.Source> = emptySet(),
    onlySubscribed: Boolean = true,
): Flow<List<TrackReference>> {
    return participant::trackPublications.flow
        .mapLatest { participant.getTrackReferencesBySource(sources, usePlaceholders, onlySubscribed) }
}

fun Participant.getTrackReferencesBySource(
    sources: List<Track.Source>,
    usePlaceholders: Set<Track.Source> = emptySet(),
    onlySubscribed: Boolean = true
): List<TrackReference> {
    return sources.flatMap { source ->
        // Get all tracks for source
        var tracks = trackPublications.values.mapNotNull { trackPub ->
            if (trackPub.source == source &&
                (!onlySubscribed || trackPub.subscribed)
            ) {
                TrackReference(
                    participant = this,
                    publication = trackPub,
                    source = trackPub.source
                )
            } else {
                null
            }
        }

        // If no tracks exist for source, create a placeholder.
        if (tracks.isEmpty() && usePlaceholders.contains(source)) {
            // Add placeholder
            tracks = listOf(
                TrackReference(
                    participant = this,
                    publication = null,
                    source = source,
                )
            )
        }
        tracks
    }
}
