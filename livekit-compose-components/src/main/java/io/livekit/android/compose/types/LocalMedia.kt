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

import com.twilio.audioswitch.AudioDevice
import io.livekit.android.annotations.Beta
import io.livekit.android.room.track.screencapture.ScreenCaptureParams
import livekit.org.webrtc.CameraEnumerator

/**
 * A representation of all the media devices available to the local participant,
 * as well as helper methods for easier access for controlling those devices.
 */
@Beta
abstract class LocalMedia {
    /**
     * The local participant's microphone track if one is published.
     */
    abstract val microphoneTrack: TrackReference?

    /**
     * The local participant's camera track if one is published.
     */
    abstract val cameraTrack: TrackReference?

    /**
     * The local participant's screen share track if one is published.
     */
    abstract val screenShareTrack: TrackReference?

    /**
     * Whether the microphone is enabled.
     */
    abstract val isMicrophoneEnabled: Boolean

    /**
     * Whether the camera is enabled.
     */
    abstract val isCameraEnabled: Boolean

    /**
     * Whether the screenshare is enabled.
     */
    abstract val isScreenShareEnabled: Boolean

    /**
     * The list of available audio devices.
     */
    abstract val audioDevices: List<AudioDevice>

    /**
     * The list of available camera devices.
     */
    abstract val cameraDevices: List<String>

    /**
     * The current audio device in use, if it exists.
     * @see selectAudioDevice
     */
    abstract val selectedAudioDevice: AudioDevice?

    /**
     * The id of the current camera in use, if it exists.
     * @see selectCamera
     */
    abstract val selectedCameraId: String?

    /**
     * Whether the camera can switch position (i.e. from front facing to back facing camera, and vice-versa).
     * @see switchCamera
     */
    abstract val cameraCanSwitchPosition: Boolean

    internal abstract val cameraEnumerator: CameraEnumerator

    /**
     * Starts the microphone track and publishes it if needed.
     */
    abstract suspend fun startMicrophone()

    /**
     * Stops the microphone.
     */
    abstract suspend fun stopMicrophone()

    /**
     * Starts the camera track and publishes it if needed.
     */
    abstract suspend fun startCamera()

    /**
     * Stops the camera.
     */
    abstract suspend fun stopCamera()

    /**
     * Starts screensharing track and publishes it if needed. The result intent from starting a
     * [android.media.projection.MediaProjectionManager.createScreenCaptureIntent] is required.
     *
     * @see android.media.projection.MediaProjectionManager.createScreenCaptureIntent
     */
    abstract suspend fun startScreenShare(params: ScreenCaptureParams)

    /**
     * Stops screensharing.
     */
    abstract suspend fun stopScreenShare()

    /**
     * Selects the audio device to be used.
     */
    abstract fun selectAudioDevice(audioDevice: AudioDevice)

    /**
     * Selects the camera to be used.
     */
    abstract fun selectCamera(deviceName: String)

    /**
     * Switches the position of the camera, if able.
     * @see switchCamera
     */
    abstract fun switchCamera()
}
