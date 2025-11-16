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

package io.livekit.android.compose.state.transcriptions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import io.livekit.android.annotations.Beta
import io.livekit.android.compose.flow.DataTopic
import io.livekit.android.compose.flow.TextStreamData
import io.livekit.android.compose.local.requireParticipant
import io.livekit.android.compose.local.requireRoom
import io.livekit.android.compose.stream.rememberTextStream
import io.livekit.android.compose.types.TrackReference
import io.livekit.android.compose.util.rememberStateOrDefault
import io.livekit.android.room.Room
import io.livekit.android.room.participant.Participant
import io.livekit.android.util.flow

/**
 * Collect all the transcriptions for the room.
 *
 * @return Returns the collected transcriptions, ordered by timestamp.
 */
@Beta
@Composable
fun rememberTranscriptions(
    room: Room? = null,
    participantIdentities: List<Participant.Identity>? = null,
    trackSids: List<String>? = null,
): State<List<TextStreamData>> {
    val room = requireRoom(room)
    val textStreams by rememberTextStream(room = room, topic = DataTopic.TRANSCRIPTION.value)

    val filteredTextStreams = remember {
        derivedStateOf {
            textStreams
                .filter { streamData ->
                    participantIdentities?.contains(streamData.participantIdentity)
                        ?: true
                }
                .filter { streamData ->
                    trackSids?.contains(streamData.streamInfo.attributes["lk.transcribed_track_id"])
                        ?: true
                }
        }
    }

    return filteredTextStreams
}

/**
 * Collect all the transcriptions for a track reference.
 *
 * @return Returns the collected transcriptions, ordered by timestamp.
 */
@Beta
@Composable
fun rememberTrackTranscriptions(trackReference: TrackReference, room: Room? = null): State<List<TextStreamData>> {
    val publication = trackReference.publication
    return rememberStateOrDefault(emptyList()) {
        if (publication == null) {
            null
        } else {
            rememberTranscriptions(
                trackSids = listOf(publication.sid),
                room = room
            )
        }
    }
}

/**
 * Collect all the transcriptions for a participant.
 *
 * @return Returns the collected transcriptions, ordered by timestamp.
 */
@Beta
@Composable
fun rememberParticipantTranscriptions(passedParticipant: Participant? = null, room: Room? = null): State<List<TextStreamData>> {
    val participant = requireParticipant(passedParticipant)
    val identity = participant::identity.flow.collectAsState().value

    return rememberUpdatedState(
        if (identity == null) {
            emptyList()
        } else {
            rememberTranscriptions(
                participantIdentities = listOf(identity),
                room = room,
            ).value
        }
    )
}