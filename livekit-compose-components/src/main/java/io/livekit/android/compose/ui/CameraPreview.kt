package io.livekit.android.compose.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.currentCompositeKeyHash
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import io.livekit.android.renderer.TextureViewRenderer
import io.livekit.android.room.track.CameraPosition
import org.webrtc.Camera1Enumerator
import org.webrtc.Camera2Enumerator
import org.webrtc.CameraVideoCapturer
import org.webrtc.CapturerObserver
import org.webrtc.EglBase
import org.webrtc.RendererCommon
import org.webrtc.SurfaceTextureHelper
import org.webrtc.VideoFrame

/**
 * A standalone camera preview composable that can be used without a Room object.
 *
 * Due to hardware limitations, this should not be used while any camera is in use, or it may fail.
 */
@Composable
fun CameraPreview(cameraPosition: CameraPosition, modifier: Modifier = Modifier, mirror: Boolean = false) {

    val context = LocalContext.current

    val eglBaseContext = remember { EglBase.create().eglBaseContext }
    val cameraEnumerator = remember {
        if (Camera2Enumerator.isSupported(context)) {
            Camera2Enumerator(context)
        } else {
            Camera1Enumerator()
        }
    }

    var view: TextureViewRenderer? by remember { mutableStateOf(null) }

    DisposableEffect(cameraPosition) {
        val deviceName = cameraEnumerator
            .deviceNames
            .firstOrNull { name ->
                when (cameraPosition) {
                    CameraPosition.FRONT -> cameraEnumerator.isFrontFacing(name)
                    CameraPosition.BACK -> cameraEnumerator.isBackFacing(name)
                }
            }

        var capturer: CameraVideoCapturer? = null
        if (deviceName != null) {
            val createdCapturer = cameraEnumerator.createCapturer(deviceName, object : CameraVideoCapturer.CameraEventsHandler {
                override fun onCameraError(p0: String?) {
                }

                override fun onCameraDisconnected() {
                }

                override fun onCameraFreezed(p0: String?) {
                }

                override fun onCameraOpening(p0: String?) {
                }

                override fun onFirstFrameAvailable() {
                }

                override fun onCameraClosed() {
                }
            })

            val surfaceTextureHelper = SurfaceTextureHelper.create("VideoCaptureThread", eglBaseContext)
            createdCapturer.initialize(
                surfaceTextureHelper,
                context,
                object : CapturerObserver {
                    override fun onCapturerStarted(started: Boolean) {
                    }

                    override fun onCapturerStopped() {
                    }

                    override fun onFrameCaptured(frame: VideoFrame) {
                        view?.onFrame(frame)
                    }
                }
            )

            createdCapturer.startCapture(1280, 720, 30)

            capturer = createdCapturer
        }

        onDispose {
            capturer?.stopCapture()
        }
    }

    DisposableEffect(view, mirror) {
        view?.setMirror(mirror)
        onDispose { }
    }

    DisposableEffect(currentCompositeKeyHash.toString()) {
        onDispose {
            view?.release()
        }
    }

    AndroidView(
        factory = {
            TextureViewRenderer(context).apply {
                this.init(eglBaseContext, null)
                this.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)
                this.setEnableHardwareScaler(false)
                view = this
            }
        },
        modifier = modifier
    )
}

fun CameraPosition.flipped() = when (this) {
    CameraPosition.FRONT -> CameraPosition.BACK
    CameraPosition.BACK -> CameraPosition.FRONT
}