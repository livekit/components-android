/*
 * Copyright 2023-2024 LiveKit, Inc.
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
import io.livekit.android.compose.local.RoomLocal
import io.livekit.android.compose.local.requireRoom
import io.livekit.android.compose.types.TrackReference
import io.livekit.android.room.Room
import io.livekit.android.room.participant.Participant
import io.livekit.android.room.track.Track
import io.livekit.android.room.track.TrackPublication
import io.livekit.android.util.flow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest

/**
 * Returns an array of TrackReferences depending the sources provided.
 *
 * @param sources The sources of the tracks to provide. Defaults to camera and screen share tracks.
 * @param usePlaceholders A set of sources to provide placeholders for.
 *     A placeholder will provide a TrackReference for participants that don't
 *     yet have a track published for that source. Defaults to no placeholders.
 * @param passedRoom The room to use on, or [RoomLocal] if null/not passed.
 * @param onlySubscribed If true, only return tracks that have been subscribed. Defaults to true.
 */
@Composable
fun rememberTracks(
    sources: List<Track.Source> = listOf(
        Track.Source.CAMERA,
        Track.Source.SCREEN_SHARE
    ),
    usePlaceholders: Set<Track.Source> = emptySet(),
    passedRoom: Room? = null,
    onlySubscribed: Boolean = true,
): List<TrackReference> {
    val room = requireRoom(passedRoom)

    return trackReferencesFlow(
        room = room,
        sources = sources,
        usePlaceholders = usePlaceholders,
        onlySubscribed = onlySubscribed
    )
        .collectAsState(initial = room.getTrackReferences(sources, usePlaceholders, onlySubscribed))
        .value
}

/**
 * A flow of the TrackReferences/placeholders in the room.
 *
 * @see rememberTracks
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal fun trackReferencesFlow(
    room: Room,
    sources: List<Track.Source>,
    usePlaceholders: Set<Track.Source> = emptySet(),
    onlySubscribed: Boolean = true,
): Flow<List<TrackReference>> {
    return room::remoteParticipants.flow
        .flatMapLatest { remoteParticipants ->
            val allParticipants = listOf(room.localParticipant).plus(remoteParticipants.values)

            // Get flows of all the participant's trackPublications.
            // Although we don't use the trackPublications directly here,
            // We tie into the flow so we can be sensitive to its changes,
            // due to participants being mutable.
            val participantToTrackPubFlows = allParticipants.map { participant ->
                participant::trackPublications.flow
                    .trackUpdateFlow()
                    .mapLatest { list -> list.map { (pub, _) -> pub } }
                    .mapLatest { trackPublications ->
                        participant to trackPublications
                    }
            }

            // Flat map each participant into to track references.
            return@flatMapLatest combine(participantToTrackPubFlows) { participantToTrackPubList ->
                participantToTrackPubList.flatMap { (participant, trackPubs) ->
                    calculateTrackReferences(
                        participant = participant,
                        trackPublications = trackPubs,
                        sources = sources,
                        usePlaceholders = usePlaceholders,
                        onlySubscribed = onlySubscribed
                    )
                }
            }
        }
}

/**
 * @see rememberTracks
 */
fun Room.getTrackReferences(
    sources: List<Track.Source>,
    usePlaceholders: Set<Track.Source> = emptySet(),
    onlySubscribed: Boolean = true
): List<TrackReference> {
    val allParticipants = listOf(localParticipant).plus(remoteParticipants.values)
    return allParticipants.flatMap { participant ->
        participant.getTrackReferencesBySource(sources, usePlaceholders, onlySubscribed)
    }
}

internal fun calculateTrackReferences(
    participant: Participant,
    trackPublications: Collection<TrackPublication>,
    sources: List<Track.Source>,
    usePlaceholders: Set<Track.Source> = emptySet(),
    onlySubscribed: Boolean = true
): List<TrackReference> {
    return sources.flatMap { source ->
        // Get all tracks for source
        var tracks = trackPublications.mapNotNull { trackPub ->
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

        // If no tracks exist for source, create a placeholder.
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
        tracks
    }
}

internal fun Flow<Map<String, TrackPublication>>.trackUpdateFlow(): Flow<List<Pair<TrackPublication, Track?>>> {
    return flatMapLatest { publicationMap ->
        if (publicationMap.isEmpty()) {
            flowOf(emptyList())
        } else {
            combine(
                publicationMap.values
                    .map { trackPublication ->
                        // Re-emit when track changes
                        trackPublication::track.flow
                            .map { trackPublication to trackPublication.track }
                    },
            ) { trackPubs ->
                trackPubs.toList()
            }
        }
    }
}