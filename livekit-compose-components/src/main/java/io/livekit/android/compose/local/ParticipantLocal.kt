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
import androidx.compose.runtime.compositionLocalOf
import io.livekit.android.room.participant.LocalParticipant
import io.livekit.android.room.participant.Participant

/**
 * Not to be confused with [LocalParticipant].
 */
val ParticipantLocal =
    compositionLocalOf<Participant> { throw IllegalStateException("No Participant object available. This should only be used within a ParticipantScope.") }

@Composable
fun ParticipantScope(
    participant: Participant,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        ParticipantLocal provides participant,
        content = content,
    )
}

/**
 * Returns the [passedParticipant] or the currently provided [ParticipantLocal].
 * @throws IllegalStateException if passedParticipant is null and no ParticipantLocal is available (e.g. not inside a [RoomScope]).
 */
@Composable
@Throws(IllegalStateException::class)
fun requireParticipant(passedParticipant: Participant? = null): Participant {
    return passedParticipant ?: ParticipantLocal.current
}
