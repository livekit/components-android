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

package io.livekit.android.compose.flow

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import io.livekit.android.events.EventListenable
import io.livekit.android.events.ParticipantEvent
import io.livekit.android.events.RoomEvent
import io.livekit.android.events.TrackEvent
import io.livekit.android.events.TrackPublicationEvent
import io.livekit.android.events.collect
import io.livekit.android.room.Room
import io.livekit.android.room.participant.Participant
import io.livekit.android.room.track.Track
import io.livekit.android.room.track.TrackPublication
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * A utility method to obtain a flow for specific room events.
 *
 * Pass in [RoomEvent] as the type to receive all room events.
 *
 * ```
 * // Receive only participant connected events.
 * val eventFlow = rememberEventSelector<RoomEvent.ParticipantConnected>(room = room)
 * ```
 */
@Composable
inline fun <reified T : RoomEvent> rememberEventSelector(room: Room): Flow<T> {
    return rememberEventSelector<T, RoomEvent>(eventListenable = room.events)
}

/**
 * A utility method to obtain a flow for specific room events.
 *
 * Pass in [ParticipantEvent] as the type to receive all room events.
 *
 * ```
 * // Receive only participant connected events.
 * val eventFlow = rememberEventSelector<ParticipantEvent.SpeakingChanged>(participant)
 * ```
 */
@Composable
inline fun <reified T : ParticipantEvent> rememberEventSelector(participant: Participant): Flow<T> {
    return rememberEventSelector<T, ParticipantEvent>(eventListenable = participant.events)
}

/**
 * A utility method to obtain a flow for specific room events.
 *
 * Pass in [TrackPublicationEvent] as the type to receive all publication events.
 *
 * ```
 * // Receive only participant connected events.
 * val eventFlow = rememberEventSelector<TrackPublicationEvent.TranscriptionReceived>(publication)
 * ```
 */
@Composable
inline fun <reified T : TrackPublicationEvent> rememberEventSelector(publication: TrackPublication): Flow<T> {
    return rememberEventSelector<T, TrackPublicationEvent>(eventListenable = publication.events)
}

/**
 * A utility method to obtain a flow for specific track events.
 *
 * Pass in [TrackEvent] as the type to receive all track events.
 *
 * ```
 * // Receive only stream state changed events.
 * val eventFlow = rememberEventSelector<TrackEvent.StreamStateChanged>(track)
 * ```
 */
@Composable
inline fun <reified T : TrackEvent> rememberEventSelector(track: Track): Flow<T> {
    return rememberEventSelector<T, TrackEvent>(eventListenable = track.events)
}

/**
 * A utility method to obtain a flow for events.
 *
 * ```
 * // Receive only stream state changed events.
 * val eventFlow = rememberEventSelector<TrackEvent.StreamStateChanged>(track.events)
 * ```
 */
@Composable
inline fun <reified T, U> rememberEventSelector(eventListenable: EventListenable<U>): Flow<T>
    where T : U {
    val flow = remember(eventListenable) {
        MutableSharedFlow<T>(extraBufferCapacity = 100)
    }

    LaunchedEffect(eventListenable) {
        eventListenable.collect {
            if (it is T) {
                flow.emit(it)
            }
        }
    }

    return flow
}
