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
