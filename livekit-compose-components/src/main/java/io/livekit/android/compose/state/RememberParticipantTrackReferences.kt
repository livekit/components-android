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
import io.livekit.android.compose.local.ParticipantLocal
import io.livekit.android.compose.local.RoomLocal
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

/**
 * Returns an array of TrackReferences for a participant depending the sources provided.
 *
 * @param sources The sources of the tracks to provide. Defaults to camera and screen share tracks.
 * @param participantIdentity The identity of the participant.
 * @param usePlaceholders A set of sources to provide placeholders for.
 *     A placeholder will provide a TrackReference for participants that don't
 *     yet have a track published for that source. Defaults to no placeholders.
 * @param passedRoom The room to use on, or [RoomLocal] if null.
 * @param onlySubscribed If true, only return tracks that have been subscribed. Defaults to true.
 */
@Composable
fun rememberParticipantTrackReferences(
    sources: List<Track.Source>,
    participantIdentity: Participant.Identity? = null,
    passedRoom: Room? = null,
    usePlaceholders: Set<Track.Source> = emptySet(),
    onlySubscribed: Boolean = true,
): List<TrackReference> {
    val room = requireRoom(passedRoom)
    val participant = if (participantIdentity != null) {
        room.getParticipantByIdentity(participantIdentity)
    } else {
        null
    }

    return rememberParticipantTrackReferences(
        sources = sources,
        passedParticipant = participant,
        usePlaceholders = usePlaceholders,
        onlySubscribed = onlySubscribed
    )
}

/**
 * Returns an array of TrackReferences for a participant depending the sources provided.
 *
 * @param sources The sources of the tracks to provide. Defaults to camera and screen share tracks.
 * @param usePlaceholders A set of sources to provide placeholders for.
 *     A placeholder will provide a TrackReference for participants that don't
 *     yet have a track published for that source. Defaults to no placeholders.
 * @param passedParticipant The participant to use on, or [ParticipantLocal] if null/not passed.
 * @param onlySubscribed If true, only return tracks that have been subscribed. Defaults to true.
 */
@Composable
fun rememberParticipantTrackReferences(
    sources: List<Track.Source> = listOf(
        Track.Source.CAMERA,
        Track.Source.SCREEN_SHARE
    ),
    usePlaceholders: Set<Track.Source> = emptySet(),
    passedParticipant: Participant? = null,
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
 * A flow of the TrackReferences/placeholders for a participant.
 *
 * @see rememberTracks
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal fun participantTrackReferencesFlow(
    participant: Participant,
    sources: List<Track.Source>,
    usePlaceholders: Set<Track.Source> = emptySet(),
    onlySubscribed: Boolean = true,
): Flow<List<TrackReference>> {
    return participant::trackPublications.flow
        .trackUpdateFlow()
        .mapLatest { list -> list.map { (pub, _) -> pub } }
        .mapLatest { trackPubs ->
            calculateTrackReferences(
                participant = participant,
                trackPublications = trackPubs,
                sources = sources,
                usePlaceholders = usePlaceholders,
                onlySubscribed = onlySubscribed,
            )
        }
}

/**
 * @see rememberParticipantTrackReferences
 */
fun Participant.getTrackReferencesBySource(
    sources: List<Track.Source>,
    usePlaceholders: Set<Track.Source> = emptySet(),
    onlySubscribed: Boolean = true
): List<TrackReference> {
    return calculateTrackReferences(
        participant = this,
        trackPublications = this.trackPublications.values,
        sources = sources,
        usePlaceholders = usePlaceholders,
        onlySubscribed = onlySubscribed,
    )
}
