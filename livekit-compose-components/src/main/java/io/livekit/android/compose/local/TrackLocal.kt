/*
 * Copyright 2023 LiveKit, Inc.
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

package io.livekit.android.compose.local

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import io.livekit.android.room.participant.Participant
import io.livekit.android.room.track.Track
import io.livekit.android.room.track.TrackPublication
import io.livekit.android.room.track.VideoTrack
import io.livekit.android.util.flow
import kotlinx.coroutines.flow.collectLatest

val TrackLocal =
    compositionLocalOf<Track> { throw IllegalStateException("No Track object available. This should only be used within a TrackScope.") }

@Composable
fun TrackScope(
    track: Track,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        TrackLocal provides track,
        content = content,
    )
}

/**
 * Finds the appropriate video track for a participant.
 *
 * @param participant The participant to grab video publications from
 * @param sources The priority order of [Track.Source] to search for. Defaults to screen share and then camera.
 * Pass an empty list to have no priority.
 * @param predicate A custom predicate to test which publication to grab.
 */
@Composable
fun rememberVideoTrackPublication(
    participant: Participant?,
    sources: List<Track.Source> = listOf(Track.Source.SCREEN_SHARE, Track.Source.CAMERA),
    predicate: (TrackPublication) -> Boolean = { false }
): TrackPublication? {
    val trackPubState = remember { mutableStateOf<TrackPublication?>(null) }

    LaunchedEffect(participant) {
        if (participant == null) {
            trackPubState.value = null
        } else {
            participant::videoTracks.flow.collectLatest { videoTrackMap ->
                val videoPubs = videoTrackMap.filter { (pub) -> pub.subscribed }
                    .map { (pub) -> pub }

                val videoPub = run {
                    val predicates = sources.map { source -> { pub: TrackPublication -> pub.source == source } }
                        .plus { pub -> predicate(pub) }
                        .plus { true } // return first available video track.

                    for (p in predicates) {
                        val pub = videoPubs.firstOrNull(p)
                        if (pub != null) {
                            return@run pub
                        }
                    }

                    return@run null
                }
                trackPubState.value = videoPub
            }
        }
    }

    return trackPubState.value
}
