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

package io.livekit.android.compose.ui.audio

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.livekit.android.annotations.Beta
import io.livekit.android.compose.state.AgentState
import io.livekit.android.compose.state.VoiceAssistant
import io.livekit.android.compose.state.rememberVoiceAssistant
import io.livekit.android.compose.types.TrackReference
import io.livekit.android.compose.ui.defaultBarVisualizerAnimationSpec
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

private const val ON = 1.0f
private const val OFF = 0.3f

/**
 * An audio visualizer for a [VoiceAssistant].
 *
 * @see [rememberVoiceAssistant]
 */
@Beta
@Composable
fun VoiceAssistantBarVisualizer(
    voiceAssistant: VoiceAssistant,
    modifier: Modifier = Modifier,
    barCount: Int = 15,
    loPass: Int = 50,
    hiPass: Int = 150,
    brush: Brush = SolidColor(Color.Black),
    barWidth: Dp = 8.dp,
    minHeight: Float = 0.2f,
    maxHeight: Float = 1f,
    animationSpec: AnimationSpec<Float> = defaultBarVisualizerAnimationSpec
) {
    VoiceAssistantBarVisualizer(
        agentState = voiceAssistant.state,
        audioTrackRef = voiceAssistant.audioTrack,
        modifier = modifier,
        barCount = barCount,
        loPass = loPass,
        hiPass = hiPass,
        brush = brush,
        barWidth = barWidth,
        minHeight = minHeight,
        maxHeight = maxHeight,
        animationSpec = animationSpec,
    )
}

/**
 * An audio visualizer for a [VoiceAssistant].
 *
 * @see [rememberVoiceAssistant]
 */
@Beta
@Composable
fun VoiceAssistantBarVisualizer(
    agentState: AgentState?,
    audioTrackRef: TrackReference?,
    modifier: Modifier = Modifier,
    barCount: Int = 15,
    loPass: Int = 50,
    hiPass: Int = 150,
    brush: Brush = SolidColor(Color.Black),
    barWidth: Dp = 8.dp,
    minHeight: Float = 0.2f,
    maxHeight: Float = 1f,
    animationSpec: AnimationSpec<Float> = defaultBarVisualizerAnimationSpec,
) {
    val alphas = rememberVoiceAssistantAlphas(agentState = agentState, barCount = barCount)
    AudioBarVisualizer(
        audioTrackRef = audioTrackRef,
        modifier = modifier,
        barCount = barCount,
        loPass = loPass,
        hiPass = hiPass,
        style = Fill,
        brush = brush,
        alphas = alphas,
        barWidth = barWidth,
        minHeight = minHeight,
        maxHeight = maxHeight,
        animationSpec = animationSpec,
    )
}

@Composable
private fun rememberVoiceAssistantAlphas(
    agentState: AgentState?,
    barCount: Int,
): FloatArray {
    var sequenceIndex by remember(agentState, barCount) {
        mutableIntStateOf(0)
    }
    val sequences = remember(agentState, barCount) {
        when (agentState) {
            AgentState.CONNECTING,
            AgentState.INITIALIZING -> generateConnectingSequenceBar(barCount)

            AgentState.LISTENING,
            AgentState.THINKING -> generateListeningSequenceBar(barCount)

            AgentState.SPEAKING -> generateSpeakingSequenceBar(barCount)
            else -> listOf(FloatArray(0))
        }
    }
    val intervalMs = remember(agentState) {
        when (agentState) {
            AgentState.CONNECTING -> 600L / barCount
            AgentState.INITIALIZING -> 1200L / barCount
            AgentState.LISTENING -> 500L
            AgentState.THINKING -> 150L
            AgentState.SPEAKING -> -1L
            else -> -1L
        }
    }
    LaunchedEffect(sequences, intervalMs) {
        if (intervalMs < 0) {
            return@LaunchedEffect
        }

        while (isActive) {
            delay(intervalMs)
            if (sequenceIndex == Int.MAX_VALUE) {
                sequenceIndex = Int.MIN_VALUE
            }
            sequenceIndex++
        }
    }

    return sequences[sequenceIndex % sequences.size]
}

private fun generateSpeakingSequenceBar(barCount: Int): List<FloatArray> {
    val allSequence = FloatArray(barCount) { i -> ON }
    return listOf(allSequence)
}

private fun generateConnectingSequenceBar(barCount: Int): List<FloatArray> {
    val sequences = mutableListOf<FloatArray>()
    for (i in 0 until barCount) {
        val sequence = FloatArray(barCount) { j ->
            if (j == i || j == barCount - 1 - i) {
                ON
            } else {
                OFF
            }
        }
        sequences.add(sequence)
    }
    return sequences
}

private fun generateListeningSequenceBar(barCount: Int): List<FloatArray> {
    val center = barCount / 2
    val middleBarSequence = FloatArray(barCount) { i ->
        if (i == center) {
            ON
        } else {
            OFF
        }
    }
    val goneSequence = FloatArray(barCount) { OFF }

    return listOf(middleBarSequence, goneSequence)
}
