/*
 * Copyright 2025 LiveKit, Inc.
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
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import io.livekit.android.annotations.Beta
import io.livekit.android.compose.util.rememberStateOrDefault
import io.livekit.android.room.participant.Participant
import io.livekit.android.util.flow

/**
 * Keeps track of the [AgentState] for a participant.
 */
@Beta
@Composable
fun rememberAgentState(participant: Participant?): State<AgentState> {
    return rememberStateOrDefault(AgentState.DISCONNECTED) {
        if (participant != null) {
            val agentSdkState by participant::agentAttributes.flow.collectAsState()
            rememberUpdatedState(AgentState.fromAgentSdkState(agentSdkState.lkAgentState))
        } else {
            null
        }
    }
}
