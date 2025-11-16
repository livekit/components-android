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

package io.livekit.android.compose.types

import androidx.compose.runtime.snapshots.SnapshotStateList
import com.twilio.audioswitch.AudioDevice
import io.livekit.android.room.track.screencapture.ScreenCaptureParams
import livekit.org.webrtc.CameraEnumerator

interface LocalMedia {
    val microphoneTrack: TrackReference?
    val cameraTrack: TrackReference?
    val screenShareTrack: TrackReference?

    val isMicrophoneEnabled: Boolean
    val isCameraEnabled: Boolean
    val isScreenShareEnabled: Boolean

    val audioDevices: List<AudioDevice>
    val cameraDevices: SnapshotStateList<String>
    val selectedAudioDevice: AudioDevice?
    val selectedCameraId: String?
    val canSwitchPosition: Boolean

    val cameraEnumerator: CameraEnumerator

    suspend fun startMicrophone()
    suspend fun stopMicrophone()
    suspend fun startCamera()
    suspend fun stopCamera()
    suspend fun startScreenShare(params: ScreenCaptureParams)
    suspend fun stopScreenShare()
    fun selectAudioDevice(audioDevice: AudioDevice)
    fun selectCamera(deviceName: String)
    fun switchCamera()
}
