package io.livekit.android.compose.ui

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
import io.livekit.android.renderer.TextureViewRenderer
import io.livekit.android.room.Room
import io.livekit.android.room.track.RemoteVideoTrack
import io.livekit.android.room.track.VideoTrack
import org.webrtc.RendererCommon

enum class ScaleType {
    FitInside,
    Fill,
}

/**
 * Widget for displaying a VideoTrack. Handles the Compose <-> AndroidView interop needed to use
 * [TextureViewRenderer].
 */
@Composable
fun VideoRenderer(
    room: Room,
    videoTrack: VideoTrack?,
    modifier: Modifier = Modifier,
    mirror: Boolean = false,
    scaleType: ScaleType,
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

    val videoSinkVisibility = remember(room, videoTrack) { ComposeVisibility() }
    var boundVideoTrack by remember { mutableStateOf<VideoTrack?>(null) }
    var view: TextureViewRenderer? by remember { mutableStateOf(null) }

    fun cleanupVideoTrack() {
        view?.let { boundVideoTrack?.removeRenderer(it) }
        boundVideoTrack = null
    }

    fun setupVideoIfNeeded(videoTrack: VideoTrack?, view: TextureViewRenderer) {
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

    DisposableEffect(view, mirror) {
        view?.setMirror(mirror)
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
            view?.release()
        }
    }

    AndroidView(
        factory = { context ->
            TextureViewRenderer(context).apply {
                room.initVideoRenderer(this)
                setupVideoIfNeeded(videoTrack, this)

                when (scaleType) {
                    ScaleType.FitInside -> {
                        this.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT)
                    }

                    ScaleType.Fill -> {
                        this.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)
                    }
                }
                view = this
            }
        },
        update = { v -> setupVideoIfNeeded(videoTrack, v) },
        modifier = modifier
            .onGloballyPositioned { videoSinkVisibility.onGloballyPositioned(it) },
    )
}