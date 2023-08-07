package io.livekit.android.sample.livestream.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.github.ajalt.timberkt.Timber
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import io.livekit.android.compose.ui.CameraPreview
import io.livekit.android.compose.ui.flipped
import io.livekit.android.room.track.CameraPosition
import io.livekit.android.sample.livestream.destinations.RoomScreenContainerDestination
import io.livekit.android.sample.livestream.room.data.CreateStreamRequest
import io.livekit.android.sample.livestream.room.data.CreateStreamResponse
import io.livekit.android.sample.livestream.room.data.LivestreamApi
import io.livekit.android.sample.livestream.room.data.RoomMetadata
import io.livekit.android.sample.livestream.room.state.rememberEnableCamera
import io.livekit.android.sample.livestream.room.state.rememberEnableMic
import io.livekit.android.sample.livestream.room.state.requirePermissions
import io.livekit.android.sample.livestream.room.ui.ControlButton
import io.livekit.android.sample.livestream.ui.control.BackButton
import io.livekit.android.sample.livestream.ui.control.LargeTextButton
import io.livekit.android.sample.livestream.ui.control.LoadingDialog
import io.livekit.android.sample.livestream.ui.control.Spacer
import io.livekit.android.sample.livestream.ui.theme.Dimens
import io.livekit.android.sample.livestream.util.PreferencesManager
import kotlinx.coroutines.launch

/**
 * Screen for setting up options for the livestream before starting.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Destination
@Composable
fun StartScreen(
    livestreamApi: LivestreamApi,
    navigator: DestinationsNavigator,
    preferencesManager: PreferencesManager
) {
    val context = LocalContext.current
    requirePermissions(true)

    val canEnableVideo = rememberEnableCamera(enabled = true)
    val canEnableAudio = rememberEnableMic(enabled = true)
    ConstraintLayout(
        modifier = Modifier
            .padding(Dimens.spacer)
            .fillMaxSize()
    ) {
        var userName by rememberSaveable(stateSaver = TextFieldValue.Saver) {
            mutableStateOf(TextFieldValue(preferencesManager.getUsername()))
        }

        var chatEnabled by rememberSaveable {
            mutableStateOf(true)
        }

        var viewerJoinRequestEnabled by rememberSaveable {
            mutableStateOf(true)
        }
        var cameraPosition by remember {
            mutableStateOf(CameraPosition.FRONT)
        }

        val (content, joinButton) = createRefs()
        Column(modifier = Modifier
            .constrainAs(content) {
                width = Dimension.fillToConstraints
                height = Dimension.fillToConstraints
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                top.linkTo(parent.top)
                bottom.linkTo(joinButton.top)
            }
        ) {
            BackButton {
                navigator.navigateUp()
            }

            Text(
                text = "Start Livestream", fontWeight = FontWeight.W700, fontSize = 34.sp
            )

            Box(modifier = Modifier.weight(1f)) {
                if (canEnableVideo) {
                    CameraPreview(
                        cameraPosition = cameraPosition,
                        mirror = cameraPosition == CameraPosition.FRONT,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp))
                    )

                    Row {
                        Spacer(10.dp)
                        ControlButton(
                            onClick = {
                                cameraPosition = cameraPosition.flipped()
                            },
                            modifier = Modifier
                                .width(43.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                } else {
                    Text(
                        text = "Camera permissions required.",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            Spacer(Dimens.spacer)

            Text(
                text = "DETAILS",
                fontWeight = FontWeight.W700,
                fontSize = 11.sp,
                letterSpacing = 0.05.em
            )

            Spacer(4.dp)

            OutlinedTextField(
                value = userName,
                onValueChange = { userName = it },
                label = { Text("Your Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Dimens.spacer)

            Text(
                text = "OPTIONS",
                fontWeight = FontWeight.W700,
                fontSize = 11.sp,
                letterSpacing = 0.05.em
            )

            Spacer(8.dp)
            SwitchButton(
                text = "Enable chat",
                checked = chatEnabled,
                onCheckedChanged = { chatEnabled = it }
            )

            SwitchButton(
                text = "Viewers can request to join",
                checked = viewerJoinRequestEnabled,
                onCheckedChanged = { viewerJoinRequestEnabled = it }
            )

            Spacer(8.dp)

        }

        var isCreatingStream by remember { mutableStateOf(false) }
        val coroutineScope = rememberCoroutineScope()

        fun startLoad() {
            isCreatingStream = true
            coroutineScope.launch {
                var response: CreateStreamResponse? = null
                try {
                    response = livestreamApi.createStream(
                        CreateStreamRequest(
                            metadata = RoomMetadata(
                                creatorIdentity = userName.text,
                                enableChat = chatEnabled,
                                allowParticipation = viewerJoinRequestEnabled,
                            )
                        )
                    ).body()
                } catch (e: Exception) {
                    Timber.e(e) { "error" }
                }
                if (response != null) {
                    Timber.e { "response received: $response" }
                    navigator.navigate(
                        RoomScreenContainerDestination(
                            apiAuthToken = response.authToken,
                            connectionDetails = response.connectionDetails,
                            isHost = true,
                            initialCameraPosition = cameraPosition,
                        )
                    )
                } else {
                    Timber.e { "response failed!" }
                }
                isCreatingStream = false
            }
        }
        LoadingDialog(isShowingDialog = isCreatingStream)

        val joinButtonColors = ButtonDefaults.buttonColors(
            contentColor = Color.White,
            containerColor = Color(0xFFB11FF9)
        )
        LargeTextButton(
            text = "Start livestream",
            colors = joinButtonColors,
            onClick = {
                preferencesManager.setUsername(userName.text)
                if (canEnableVideo && canEnableAudio) {
                    startLoad()
                } else {
                    Toast.makeText(context, "Camera and Mic permissions are required to create a livestream.", Toast.LENGTH_LONG).show()
                }
            },
            enabled = userName.text.isNotBlank(),
            modifier = Modifier.constrainAs(joinButton) {
                width = Dimension.fillToConstraints
                height = Dimension.value(Dimens.buttonHeight)
                bottom.linkTo(parent.bottom)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
        )
    }
}


@Composable
fun SwitchButton(
    text: String,
    checked: Boolean,
    onCheckedChanged: (Boolean) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.W400,
            fontSize = 16.sp
        )

        Switch(checked = checked, onCheckedChange = onCheckedChanged)
    }
}