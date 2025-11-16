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
 * Keeps track of the agent state for a participant.
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