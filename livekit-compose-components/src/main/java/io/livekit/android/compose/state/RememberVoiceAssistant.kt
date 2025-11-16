/*
 * Copyright 2024-2025 LiveKit, Inc.
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
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import io.livekit.android.annotations.Beta
import io.livekit.android.compose.flow.TextStreamData
import io.livekit.android.compose.local.requireRoom
import io.livekit.android.compose.state.transcriptions.rememberTrackTranscriptions
import io.livekit.android.compose.types.TrackReference
import io.livekit.android.compose.util.rememberStateOrDefault
import io.livekit.android.room.Room
import io.livekit.android.room.participant.Participant
import io.livekit.android.room.participant.RemoteParticipant
import io.livekit.android.room.track.Track
import io.livekit.android.room.types.AgentSdkState
import io.livekit.android.util.flow

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
    val agent = remember {
        derivedStateOf {
            remoteParticipants.values
                .firstOrNull { p -> p.kind == Participant.Kind.AGENT }
        }
    }

    val audioTracks by rememberStateOrDefault(emptyList()) {
        val curAgent = agent.value
        if (curAgent != null) {
            rememberParticipantTrackReferences(
                sources = listOf(Track.Source.MICROPHONE),
                participantIdentity = curAgent.identity,
                passedRoom = room,
            )
        } else {
            null
        }
    }
    val audioTrack = rememberUpdatedState(audioTracks.firstOrNull())

    val agentTranscriptions = rememberStateOrDefault(emptyList()) {
        val curAudioTrack = audioTrack.value
        if (curAudioTrack != null) {
            rememberTrackTranscriptions(trackReference = curAudioTrack, room = room)
        } else {
            null
        }
    }

    val agentState = rememberAgentState(participant = agent.value)
    val agentAttributes = rememberStateOrDefault(emptyMap()) {
        val curAgent = agent.value
        if (curAgent != null) {
            curAgent::attributes.flow.collectAsState()
        } else {
            null
        }
    }

    val combinedAgentState = remember {
        derivedStateOf {
            when {
                connectionState.value == Room.State.DISCONNECTED -> {
                    AgentState.DISCONNECTED
                }

                connectionState.value == Room.State.CONNECTING -> {
                    AgentState.CONNECTING
                }

                agent.value == null -> {
                    AgentState.INITIALIZING
                }

                else -> {
                    agentState.value
                }
            }
        }
    }

    return remember {
        VoiceAssistant(
            agent = agent,
            state = combinedAgentState,
            audioTrack = audioTrack,
            agentTranscriptions = agentTranscriptions,
            agentAttributes = agentAttributes
        )
    }
}

@Stable
class VoiceAssistant(
    agent: State<RemoteParticipant?>,
    state: State<AgentState?>,
    audioTrack: State<TrackReference?>,
    agentTranscriptions: State<List<TextStreamData>>,
    agentAttributes: State<Map<String, String>>,
) {
    val agent by agent
    val state by state
    val audioTrack by audioTrack
    val agentTranscriptions by agentTranscriptions
    val agentAttributes by agentAttributes
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
    FAILED,
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
