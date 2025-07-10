/*
 * Copyright 2023-2025 LiveKit, Inc.
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

import android.view.Gravity
import android.view.SurfaceView
import android.view.TextureView
import android.view.View
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.currentCompositeKeyHash
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.viewinterop.AndroidView
import io.livekit.android.compose.local.requireRoom
import io.livekit.android.compose.state.rememberTrack
import io.livekit.android.compose.types.TrackReference
import io.livekit.android.renderer.SurfaceViewRenderer
import io.livekit.android.renderer.TextureViewRenderer
import io.livekit.android.room.Room
import io.livekit.android.room.track.RemoteVideoTrack
import io.livekit.android.room.track.VideoTrack
import livekit.org.webrtc.RendererCommon
import livekit.org.webrtc.RendererCommon.RendererEvents
import livekit.org.webrtc.VideoSink

/**
 * The type of scaling to use with [VideoTrackView]
 */
enum class ScaleType {
    FitInside,
    Fill,
}

/**
 * Widget for displaying a VideoTrack. Handles the Compose <-> AndroidView interop needed to use
 * [TextureViewRenderer].
 */
@Composable
fun VideoTrackView(
    trackReference: TrackReference?,
    modifier: Modifier = Modifier,
    room: Room? = null,
    mirror: Boolean = false,
    scaleType: ScaleType = ScaleType.Fill,
    rendererType: RendererType = RendererType.Texture,
    onFirstFrameRendered: () -> Unit = {}
) {
    val track = if (trackReference != null) {
        rememberTrack<VideoTrack>(trackIdentifier = trackReference)
    } else {
        null
    }

    VideoTrackView(
        videoTrack = track,
        modifier = modifier,
        passedRoom = room,
        mirror = mirror,
        scaleType = scaleType,
        rendererType = rendererType,
        onFirstFrameRendered = onFirstFrameRendered,
    )
}

/**
 * Widget for displaying a VideoTrack. Handles the Compose <-> AndroidView interop needed to use
 * [TextureViewRenderer].
 */
@Composable
fun VideoTrackView(
    videoTrack: VideoTrack?,
    modifier: Modifier = Modifier,
    passedRoom: Room? = null,
    mirror: Boolean = false,
    scaleType: ScaleType = ScaleType.Fill,
    rendererType: RendererType = RendererType.Texture,
    onFirstFrameRendered: (() -> Unit)? = null
) {
    // Show a black box for preview.
    if (LocalView.current.isInEditMode) {
        Box(
            modifier = Modifier
                .background(Color.Black)
                .then(modifier)
        )
        return
    }

    val room = requireRoom(passedRoom)

    val videoSinkVisibility = remember(room, videoTrack) { ComposeVisibility() }
    var boundVideoTrack by remember { mutableStateOf<VideoTrack?>(null) }
    var rendererCompat: RendererCompat? by remember { mutableStateOf(null) }
    val firstFrameRenderCallbackState = remember { mutableStateOf(onFirstFrameRendered) }.apply { value = onFirstFrameRendered }

    var rendererEvents by remember {
        mutableStateOf(object : RendererEvents {
            override fun onFirstFrameRendered() {
                firstFrameRenderCallbackState.value?.invoke()
            }

            override fun onFrameResolutionChanged(p0: Int, p1: Int, p2: Int) {
                // Intentionally left blank.
            }
        })
    }

    fun cleanupVideoTrack() {
        rendererCompat?.let { boundVideoTrack?.removeRenderer(it.sink) }
        boundVideoTrack = null
    }

    fun setupVideoIfNeeded(videoTrack: VideoTrack?, view: VideoSink) {
        if (boundVideoTrack == videoTrack) {
            return
        }

        cleanupVideoTrack()

        boundVideoTrack = videoTrack
        if (videoTrack != null) {
            if (videoTrack is RemoteVideoTrack) {
                videoTrack.addRenderer(view, videoSinkVisibility)
            } else {
                videoTrack.addRenderer(view)
            }
        }
    }

    DisposableEffect(rendererCompat, mirror) {
        rendererCompat?.setMirror(mirror)
        onDispose { }
    }

    DisposableEffect(room, videoTrack) {
        onDispose {
            videoSinkVisibility.onDispose()
            cleanupVideoTrack()
        }
    }

    DisposableEffect(currentCompositeKeyHash.toString()) {
        onDispose {
            rendererCompat?.release()
        }
    }

    fun updateView(v: View) {
        val compat = v.tag as RendererCompat
        setupVideoIfNeeded(videoTrack, compat.sink)

        when (scaleType) {
            ScaleType.FitInside -> {
                compat.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT)
            }

            ScaleType.Fill -> {
                compat.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)
            }
        }
    }

    when (rendererType) {
        RendererType.Surface -> {
            AndroidView(
                factory = { context ->
                    FrameLayout(context).apply {
                        val renderer = SurfaceViewRenderer(context).apply {
                            init(room.lkObjects.eglBase.eglBaseContext, rendererEvents)
                            setupVideoIfNeeded(videoTrack, this)
                        }
                        addView(
                            renderer,
                            FrameLayout.LayoutParams(
                                FrameLayout.LayoutParams.WRAP_CONTENT,
                                FrameLayout.LayoutParams.WRAP_CONTENT,
                                Gravity.CENTER
                            )
                        )

                        val compat = SurfaceRendererCompat(renderer)
                        tag = compat
                        rendererCompat = compat
                    }
                },
                update = { v ->
                    updateView(v)
                },
                modifier = modifier
                    .onGloballyPositioned { videoSinkVisibility.onGloballyPositioned(it) },
            )
        }

        RendererType.Texture -> {
            AndroidView(
                factory = { context ->
                    FrameLayout(context).apply {
                        val renderer = TextureViewRenderer(context).apply {
                            init(room.lkObjects.eglBase.eglBaseContext, rendererEvents)
                            setupVideoIfNeeded(videoTrack, this)
                        }
                        addView(
                            renderer,
                            FrameLayout.LayoutParams(
                                FrameLayout.LayoutParams.WRAP_CONTENT,
                                FrameLayout.LayoutParams.WRAP_CONTENT,
                                Gravity.CENTER
                            )
                        )

                        val compat = TextureRendererCompat(renderer)
                        tag = compat
                        rendererCompat = compat
                    }
                },
                update = { v -> updateView(v) },
                modifier = modifier
                    .onGloballyPositioned { videoSinkVisibility.onGloballyPositioned(it) },
            )
        }
    }
}

enum class RendererType {
    /**
     * Use a [SurfaceView] for rendering. This is more energy efficient and performant, but may have issues
     * with view modifiers (i.e. clipping, rotating, scaling).
     */
    Surface,

    /**
     * Use a [TextureView] for rendering. This is more flexible with composing and various view modifiers
     * (i.e. clipping, rotating, scaling), but is less efficient and may cause more battery drain.
     */
    Texture
}

private const val RENDERER_COMPAT_TAG = 54376

private interface RendererCompat {
    val view: View
    val sink: VideoSink
    fun setScalingType(type: RendererCommon.ScalingType)
    fun setMirror(mirror: Boolean)
    fun release()
}

private class SurfaceRendererCompat(override val view: SurfaceViewRenderer) : RendererCompat {
    override val sink: VideoSink
        get() = view

    override fun setScalingType(type: RendererCommon.ScalingType) {
        view.setScalingType(type)
    }

    override fun setMirror(mirror: Boolean) {
        view.setMirror(mirror)
    }

    override fun release() {
        view.release()
    }
}

private class TextureRendererCompat(override val view: TextureViewRenderer) : RendererCompat {
    override val sink: VideoSink
        get() = view

    override fun setScalingType(type: RendererCommon.ScalingType) {
        view.setScalingType(type)
    }

    override fun setMirror(mirror: Boolean) {
        view.setMirror(mirror)
    }

    override fun release() {
        view.release()
    }
}
