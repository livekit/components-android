package io.livekit.android.sample.livestream.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.currentCompositeKeyHash
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.github.ajalt.timberkt.Timber
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import io.livekit.android.renderer.TextureViewRenderer
import io.livekit.android.room.track.CameraPosition
import io.livekit.android.sample.livestream.destinations.HostScreenContainerDestination
import io.livekit.android.sample.livestream.room.data.CreateStreamResponse
import io.livekit.android.sample.livestream.room.data.LivestreamApi
import io.livekit.android.sample.livestream.ui.control.BackButton
import io.livekit.android.sample.livestream.ui.control.LoadingDialog
import io.livekit.android.sample.livestream.ui.theme.Dimens
import kotlinx.coroutines.launch
import org.webrtc.Camera2Enumerator
import org.webrtc.CameraVideoCapturer
import org.webrtc.CameraVideoCapturer.CameraEventsHandler
import org.webrtc.CapturerObserver
import org.webrtc.EglBase
import org.webrtc.RendererCommon
import org.webrtc.SurfaceTextureHelper
import org.webrtc.VideoFrame

@Destination
@Composable
fun StartPreviewScreen(
    name: String,
    roomName: String,
    enableChat: Boolean,
    allowParticipation: Boolean,
    livestreamApi: LivestreamApi,
    navigator: DestinationsNavigator
) {
    ConstraintLayout(
        modifier = Modifier
            .padding(Dimens.spacer)
            .fillMaxSize()
    ) {
        val (backButton, title, cameraPreview, explanationText, startButton) = createRefs()

        Box(modifier = Modifier.constrainAs(cameraPreview) {
            width = Dimension.fillToConstraints
            height = Dimension.fillToConstraints
            start.linkTo(parent.start)
            end.linkTo(parent.end)
            top.linkTo(title.bottom, 14.dp)
            bottom.linkTo(explanationText.top, 15.dp)
        }) {
            CameraPreview(
                cameraPosition = CameraPosition.FRONT,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp))
            )
        }

        BackButton(
            modifier = Modifier.constrainAs(backButton) {
                width = Dimension.wrapContent
                height = Dimension.wrapContent
                start.linkTo(parent.start)
                top.linkTo(parent.top)
            }
        ) {
            navigator.navigateUp()
        }

        Text(
            text = "Start Livestream",
            fontWeight = FontWeight.W700,
            fontSize = 34.sp,
            modifier = Modifier.constrainAs(title) {
                width = Dimension.matchParent
                height = Dimension.wrapContent
                start.linkTo(parent.start)
                top.linkTo(backButton.bottom)
            }
        )

        Text(
            text = "After going live you’ll see a streaming link for sharing with your viewers.",
            fontSize = 13.sp,
            color = Color(0x80FFFFFF),
            modifier = Modifier.constrainAs(explanationText) {
                width = Dimension.fillToConstraints
                height = Dimension.wrapContent
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                bottom.linkTo(startButton.top, 16.dp)
            }
        )


        var isCreatingStream by remember { mutableStateOf(false) }
        val coroutineScope = rememberCoroutineScope()

        fun startLoad() {

            isCreatingStream = true
            coroutineScope.launch {
                var response: CreateStreamResponse? = null
                try {
                    response = livestreamApi.createStream(name, roomName, enableChat, allowParticipation).body()
                } catch (e: Exception) {
                    Timber.e(e) { "error" }
                }
                if (response != null) {
                    Timber.e { "response received: $response" }
                    navigator.navigate(HostScreenContainerDestination(response.livekitUrl, response.token))
                } else {
                    Timber.e { "response failed!" }
                }
                isCreatingStream = false
            }
        }

        LoadingDialog(isShowingDialog = isCreatingStream)

        val startButtonColors = ButtonDefaults.buttonColors(
            contentColor = Color.White,
            containerColor = Color(0xFFB11FF9)
        )
        Button(
            colors = startButtonColors,
            onClick = { startLoad() },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.constrainAs(startButton) {
                width = Dimension.fillToConstraints
                height = Dimension.value(Dimens.buttonHeight)
                bottom.linkTo(parent.bottom)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
        ) {
            Text(
                text = "Start livestream",
                fontSize = 17.sp,
                fontWeight = FontWeight.W700,
            )
        }

    }
}


@Composable
fun CameraPreview(cameraPosition: CameraPosition, modifier: Modifier = Modifier) {

    val context = LocalContext.current

    val eglBaseContext by remember { mutableStateOf(EglBase.create().eglBaseContext) }
    val cameraEnumerator by remember { mutableStateOf(Camera2Enumerator(context)) }
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
            val createdCapturer = cameraEnumerator.createCapturer(deviceName, object : CameraEventsHandler {
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

    DisposableEffect(currentCompositeKeyHash.toString()) {
        onDispose {
            view?.release()
        }
    }

    AndroidView(
        factory = { context ->
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