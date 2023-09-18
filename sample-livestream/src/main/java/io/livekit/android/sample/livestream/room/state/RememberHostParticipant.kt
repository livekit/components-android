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

package io.livekit.android.sample.livestream.room.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import io.livekit.android.compose.state.rememberParticipantInfo
import io.livekit.android.compose.state.rememberParticipants
import io.livekit.android.room.participant.Participant

/**
 * Finds the creator of the room.
 */
@Composable
fun rememberHostParticipant(identity: String): Participant? {
    val participantInfos = rememberParticipants().associateWith { rememberParticipantInfo(it) }

    val hostParticipant = remember(participantInfos) {
        derivedStateOf {
            participantInfos.firstNotNullOfOrNull { (participant, info) ->
                participant.takeIf {
                    info.identity == identity
                }
            }
        }
    }
    return hostParticipant.value
}
