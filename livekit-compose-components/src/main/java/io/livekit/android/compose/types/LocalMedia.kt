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

abstract class LocalMedia {
    abstract val microphoneTrack: TrackReference?
    abstract val cameraTrack: TrackReference?
    abstract val screenShareTrack: TrackReference?

    abstract val isMicrophoneEnabled: Boolean
    abstract val isCameraEnabled: Boolean
    abstract val isScreenShareEnabled: Boolean

    abstract val audioDevices: List<AudioDevice>
    abstract val cameraDevices: SnapshotStateList<String>
    abstract val selectedAudioDevice: AudioDevice?
    abstract val selectedCameraId: String?
    abstract val canSwitchPosition: Boolean

    abstract val cameraEnumerator: CameraEnumerator

    abstract suspend fun startMicrophone()
    abstract suspend fun stopMicrophone()
    abstract suspend fun startCamera()
    abstract suspend fun stopCamera()
    abstract suspend fun startScreenShare(params: ScreenCaptureParams)
    abstract suspend fun stopScreenShare()
    abstract fun selectAudioDevice(audioDevice: AudioDevice)
    abstract fun selectCamera(deviceName: String)
    abstract fun switchCamera()
}
