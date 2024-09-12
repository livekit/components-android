/*
 * Copyright 2023-2024 LiveKit, Inc.
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
import io.livekit.android.LiveKit
import io.livekit.android.renderer.TextureViewRenderer
import io.livekit.android.room.track.CameraPosition
import livekit.org.webrtc.Camera1Enumerator
import livekit.org.webrtc.Camera2Enumerator
import livekit.org.webrtc.CameraVideoCapturer
import livekit.org.webrtc.CapturerObserver
import livekit.org.webrtc.EglBase
import livekit.org.webrtc.RendererCommon
import livekit.org.webrtc.SurfaceTextureHelper
import livekit.org.webrtc.VideoFrame

/**
 * A standalone camera preview composable that can be used without a Room object.
 *
 * Due to hardware limitations, this should not be used while any camera is in use, or it may fail.
 *
 * If using this outside of a RoomScope, ensure that [LiveKit.init] is called prior to use
 * (e.g. in your Application's onCreate method).
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

/**
 * Invert the CameraPosition from front to back and vice-versa.
 */
fun CameraPosition.flipped() = when (this) {
    CameraPosition.FRONT -> CameraPosition.BACK
    CameraPosition.BACK -> CameraPosition.FRONT
}
