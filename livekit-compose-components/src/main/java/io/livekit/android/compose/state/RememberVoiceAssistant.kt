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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import io.livekit.android.annotations.Beta
import io.livekit.android.compose.local.requireRoom
import io.livekit.android.compose.state.transcriptions.rememberTrackTranscriptions
import io.livekit.android.compose.types.TrackReference
import io.livekit.android.room.Room
import io.livekit.android.room.participant.Participant
import io.livekit.android.room.participant.RemoteParticipant
import io.livekit.android.room.track.Track
import io.livekit.android.room.types.TranscriptionSegment
import io.livekit.android.util.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/**
 * This looks for the first agent-participant in the room.
 *
 * Requires an agent running with livekit-agents \>= 0.9.0.
 */
@Beta
@Composable
fun rememberVoiceAssistant(passedRoom: Room? = null): VoiceAssistant {
    val room = requireRoom(passedRoom)
    val connectionState = rememberConnectionState(room)
    val remoteParticipants by room::remoteParticipants.flow.collectAsState()
    val agent by remember {
        derivedStateOf {
            remoteParticipants.values
                .firstOrNull { p -> p.kind == Participant.Kind.AGENT }
        }
    }
    // For nullability checks
    val curAgent = agent

    val audioTrack = if (curAgent != null) {
        rememberParticipantTrackReferences(
            sources = listOf(Track.Source.MICROPHONE),
            participantIdentity = curAgent.identity,
            passedRoom = room,
        ).firstOrNull()
    } else {
        null
    }

    val agentTranscriptions = if (audioTrack != null) {
        rememberTrackTranscriptions(trackReference = audioTrack)
    } else {
        emptyList()
    }

    val agentState = rememberAgentState(participant = curAgent)
    val agentAttributes = if (curAgent != null) {
        curAgent::attributes.flow.collectAsState().value
    } else {
        emptyMap()
    }

    val combinedAgentState = remember(agentState, connectionState) {
        when {
            connectionState == Room.State.DISCONNECTED -> {
                AgentState.DISCONNECTED
            }

            connectionState == Room.State.CONNECTING -> {
                AgentState.CONNECTING
            }

            agent == null -> {
                AgentState.INITIALIZING
            }

            else -> {
                agentState
            }
        }
    }

    return remember(agent, combinedAgentState, audioTrack, agentTranscriptions, agentAttributes) {
        VoiceAssistant(
            agent = agent,
            state = combinedAgentState,
            audioTrack = audioTrack,
            agentTranscriptions = agentTranscriptions,
            agentAttributes = agentAttributes
        )
    }
}

data class VoiceAssistant(
    val agent: RemoteParticipant?,
    val state: AgentState,
    val audioTrack: TrackReference?,
    val agentTranscriptions: List<TranscriptionSegment>,
    val agentAttributes: Map<String, String>?,
)

/**
 * Keeps track of the agent state for a participant.
 */
@Composable
fun rememberAgentState(participant: Participant?): AgentState {
    val flow = remember(participant) {
        if (participant != null) {
            return@remember participant::attributes.flow
                .map { attributes -> attributes[PARTICIPANT_ATTRIBUTE_LK_AGENT_STATE_KEY] }
                .map { stateString -> AgentState.fromAttribute(stateString) }
        } else {
            return@remember flowOf(AgentState.UNKNOWN)
        }
    }

    return flow.collectAsState(initial = AgentState.UNKNOWN).value
}

const val PARTICIPANT_ATTRIBUTE_LK_AGENT_STATE_KEY = "lk.agent.state"
const val PARTICIPANT_ATTRIBUTE_LK_AGENT_STATE_INITIALIZING = "initializing"
const val PARTICIPANT_ATTRIBUTE_LK_AGENT_STATE_LISTENING = "listening"
const val PARTICIPANT_ATTRIBUTE_LK_AGENT_STATE_THINKING = "thinking"
const val PARTICIPANT_ATTRIBUTE_LK_AGENT_STATE_SPEAKING = "speaking"

enum class AgentState {
    DISCONNECTED,
    CONNECTING,
    INITIALIZING,
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING,
    UNKNOWN;

    companion object {
        fun fromAttribute(attribute: String?): AgentState {
            return when (attribute) {
                PARTICIPANT_ATTRIBUTE_LK_AGENT_STATE_INITIALIZING -> INITIALIZING
                PARTICIPANT_ATTRIBUTE_LK_AGENT_STATE_LISTENING -> LISTENING
                PARTICIPANT_ATTRIBUTE_LK_AGENT_STATE_THINKING -> THINKING
                PARTICIPANT_ATTRIBUTE_LK_AGENT_STATE_SPEAKING -> SPEAKING
                else -> UNKNOWN
            }
        }
        fun fromAgentSdkState(agentSdkState: AgentSdkState?): AgentState {
            return when (agentSdkState) {
                AgentSdkState.Idle -> IDLE
                AgentSdkState.Initializing -> INITIALIZING
                AgentSdkState.Listening -> LISTENING
                AgentSdkState.Speaking -> SPEAKING
                AgentSdkState.Thinking -> THINKING
                null -> UNKNOWN
            }
        }
    }
}