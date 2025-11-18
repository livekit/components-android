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
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateList
import io.livekit.android.annotations.Beta
import io.livekit.android.compose.local.requireSession
import io.livekit.android.compose.types.TrackReference
import io.livekit.android.compose.util.rememberStateOrDefault
import io.livekit.android.room.ConnectionState
import io.livekit.android.room.participant.RemoteParticipant
import io.livekit.android.room.participant.isAgent
import io.livekit.android.room.track.Track
import io.livekit.android.room.types.AgentAttributes
import io.livekit.android.util.flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.takeWhile

abstract class Agent {
    abstract val agentParticipant: RemoteParticipant?

    internal abstract val workerParticipant: RemoteParticipant?

    abstract val attributes: AgentAttributes?

    abstract val failureReasons: List<String>

    abstract val agentState: AgentState

    abstract val audioTrack: TrackReference?

    abstract val videoTrack: TrackReference?

    abstract val isAvailable: Boolean

    abstract val isBufferingSpeech: Boolean

    abstract suspend fun waitUntilAvailable()
    abstract suspend fun waitUntilCamera()
    abstract suspend fun waitUntilMicrophone()
}

@Stable
internal class AgentImpl(
    agentParticipantState: State<RemoteParticipant?>,
    workerParticipantState: State<RemoteParticipant?>,
    failureReasons: SnapshotStateList<String>,
    audioTrackState: State<TrackReference?>,
    videoTrackState: State<TrackReference?>,
    agentStateState: State<AgentState>,
    isAvailableState: State<Boolean>,
    isBufferingSpeechState: State<Boolean>,
    attributesState: State<AgentAttributes?>,
    private val waitUntilAvailableFn: suspend () -> Unit,
    private val waitUntilCameraFn: suspend () -> Unit,
    private val waitUntilMicrophoneFn: suspend () -> Unit,
) : Agent() {
    override val agentParticipant by agentParticipantState

    override val workerParticipant by workerParticipantState

    override val attributes by attributesState

    override val failureReasons: List<String> = failureReasons

    override val agentState by agentStateState

    override val audioTrack by audioTrackState

    override val videoTrack by videoTrackState

    override val isAvailable by isAvailableState

    override val isBufferingSpeech by isBufferingSpeechState

    override suspend fun waitUntilAvailable() {
        waitUntilAvailableFn()
    }

    override suspend fun waitUntilCamera() {
        waitUntilCameraFn()
    }

    override suspend fun waitUntilMicrophone() {
        waitUntilMicrophoneFn()
    }
}

/**
 * This looks for the first agent-participant in the room.
 *
 * Requires an agent running with livekit-agents \>= 0.9.0.
 */
@Beta
@Composable
fun rememberAgent(session: Session? = null): Agent {
    val session = requireSession(session)
    val room = session.room
    val connectionState by session::connectionState

    // Gather participant info
    val remoteParticipants by room::remoteParticipants.flow.collectAsState()
    val agentParticipantState = remember {
        derivedStateOf {
            remoteParticipants.values
                .filter { p -> p.agentAttributes.lkPublishOnBehalf == null }
                .firstOrNull { p -> p.isAgent }
        }
    }

    val workerParticipantState = remember {
        derivedStateOf {
            val curAgentParticipant = agentParticipantState.value
            if (curAgentParticipant == null) {
                return@derivedStateOf null
            }
            remoteParticipants.values
                .filter { p -> p.agentAttributes.lkPublishOnBehalf != null && p.agentAttributes.lkPublishOnBehalf == curAgentParticipant.identity?.value }
                .firstOrNull { p -> p.isAgent }
        }
    }

    // Track handling
    val agentTracks by rememberStateOrDefault(emptyList()) {
        val curAgentParticipant = agentParticipantState.value
        if (curAgentParticipant != null) {
            rememberParticipantTrackReferences(
                sources = listOf(Track.Source.MICROPHONE, Track.Source.CAMERA),
                participantIdentity = curAgentParticipant.identity,
                passedRoom = room,
            )
        } else {
            null
        }
    }

    val workerTracks by rememberStateOrDefault(emptyList()) {
        val curWorkerParticipant = workerParticipantState.value
        if (curWorkerParticipant != null) {
            rememberParticipantTrackReferences(
                sources = listOf(Track.Source.MICROPHONE, Track.Source.CAMERA),
                participantIdentity = curWorkerParticipant.identity,
                passedRoom = room,
            )
        } else {
            null
        }
    }

    val videoTrackState = remember {
        derivedStateOf {
            agentTracks.firstOrNull { trackReference -> trackReference.source == Track.Source.CAMERA }
                ?: workerTracks.firstOrNull { trackReference -> trackReference.source == Track.Source.CAMERA }
        }
    }

    val audioTrackState = remember {
        derivedStateOf {
            agentTracks.firstOrNull { trackReference -> trackReference.source == Track.Source.MICROPHONE }
                ?: workerTracks.firstOrNull { trackReference -> trackReference.source == Track.Source.MICROPHONE }
        }
    }

    val localMicTracks by rememberParticipantTrackReferences(
        sources = listOf(Track.Source.MICROPHONE),
        passedParticipant = room.localParticipant,
    )

    // Attributes and states
    val agentState by rememberAgentState(participant = agentParticipantState.value)
    val isAvailableState = remember {
        derivedStateOf {
            calculateIsAvailable(agentState)
        }
    }

    val hasAgentConnectedOnce by produceState(false, isAvailableState.value, connectionState) {
        if (connectionState == ConnectionState.DISCONNECTED) {
            value = false
        } else {
            value = value || isAvailableState.value
        }
    }
    val combinedAgentState = remember {
        derivedStateOf {
            if (connectionState == ConnectionState.DISCONNECTED) {
                return@derivedStateOf AgentState.DISCONNECTED
            }

            if (session.agentFailure != null) {
                return@derivedStateOf AgentState.FAILED
            }
            var state = AgentState.CONNECTING

            if (localMicTracks.isNotEmpty()) {
                state = AgentState.LISTENING
            }

            val agentParticipant = agentParticipantState.value
            if (agentParticipant != null) {
                state = agentState
            } else if (hasAgentConnectedOnce) {
                // means agent disconnected mid session.
                state = AgentState.DISCONNECTED
            }

            return@derivedStateOf state
        }
    }

    val isBufferingSpeech = remember {
        derivedStateOf {
            !(connectionState == ConnectionState.DISCONNECTED ||
                isAvailableState.value ||
                localMicTracks.isNotEmpty())
        }
    }

    val attributesState = rememberStateOrDefault(null) {
        val curAgentParticipant = agentParticipantState.value
        if (curAgentParticipant != null) {
            curAgentParticipant::agentAttributes.flow
                .collectAsState()
        } else {
            null
        }
    }

    // Agent actions
    val waitUntilAvailableFn = remember(room) {
        suspend waitUntilAvailable@{
            snapshotFlow { combinedAgentState.value }
                .takeWhile { !calculateIsAvailable(it) }
                .collect()
        }
    }

    val waitUntilCameraFn = remember(room) {
        suspend waitUntilCamera@{
            snapshotFlow { videoTrackState.value }
                .takeWhile { it == null }
                .collect()
        }
    }

    val waitUntilMicrophoneFn = remember(room) {
        suspend waitUntilMicrophone@{
            snapshotFlow { audioTrackState.value }
                .takeWhile { it == null }
                .collect()
        }
    }

    val failureReasons = remember(room) {
        SnapshotStateList<String>()
    }
    // Assemble the agent
    val agent = remember {
        derivedStateOf {
            AgentImpl(
                agentParticipantState = agentParticipantState,
                workerParticipantState = workerParticipantState,
                audioTrackState = audioTrackState,
                videoTrackState = videoTrackState,
                agentStateState = combinedAgentState,
                isAvailableState = isAvailableState,
                isBufferingSpeechState = isBufferingSpeech,
                failureReasons = failureReasons,
                waitUntilAvailableFn = waitUntilAvailableFn,
                waitUntilCameraFn = waitUntilCameraFn,
                waitUntilMicrophoneFn = waitUntilMicrophoneFn,
                attributesState = attributesState,
            )
        }
    }

    return agent.value
}

internal fun calculateIsAvailable(agentState: AgentState): Boolean {
    return when (agentState) {
        AgentState.IDLE,
        AgentState.LISTENING,
        AgentState.THINKING,
        AgentState.SPEAKING -> true

        AgentState.CONNECTING,
        AgentState.INITIALIZING,
        AgentState.DISCONNECTED,
        AgentState.FAILED,
        AgentState.UNKNOWN -> false
    }
}
