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

import io.livekit.android.room.track.RemoteAudioTrack
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import livekit.org.webrtc.AudioTrackSink
import java.nio.ByteBuffer

/**
 * Gathers the audio data from a [RemoteAudioTrack] and emits through a flow.
 */
class AudioTrackSinkFlow : AudioTrackSink {
    val audioFormat = MutableStateFlow(AudioFormat(16, 48000, 1))
    val audioFlow = MutableSharedFlow<Pair<ByteBuffer, Int>>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    override fun onData(
        audioData: ByteBuffer,
        bitsPerSample: Int,
        sampleRate: Int,
        numberOfChannels: Int,
        numberOfFrames: Int,
        absoluteCaptureTimestampMs: Long
    ) {
        val curAudioFormat = audioFormat.value
        if (curAudioFormat.bitsPerSample != bitsPerSample ||
            curAudioFormat.sampleRate != sampleRate ||
            curAudioFormat.numberOfChannels != numberOfChannels
        ) {
            audioFormat.tryEmit(AudioFormat(bitsPerSample, sampleRate, numberOfChannels))
        }
        audioFlow.tryEmit(audioData to numberOfFrames)
    }
}

data class AudioFormat(val bitsPerSample: Int, val sampleRate: Int, val numberOfChannels: Int)
