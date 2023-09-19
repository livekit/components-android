/*
 * Copyright 2023 LiveKit, Inc.
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

package io.livekit.android.sample.livestream.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.github.ajalt.timberkt.Timber
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import io.livekit.android.room.track.CameraPosition
import io.livekit.android.sample.livestream.destinations.RoomScreenContainerDestination
import io.livekit.android.sample.livestream.room.data.JoinStreamRequest
import io.livekit.android.sample.livestream.room.data.JoinStreamResponse
import io.livekit.android.sample.livestream.room.data.LivestreamApi
import io.livekit.android.sample.livestream.ui.control.BackButton
import io.livekit.android.sample.livestream.ui.control.LargeTextButton
import io.livekit.android.sample.livestream.ui.control.LoadingDialog
import io.livekit.android.sample.livestream.ui.control.Spacer
import io.livekit.android.sample.livestream.ui.theme.Dimens
import io.livekit.android.sample.livestream.util.PreferencesManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Destination
@Composable
fun JoinScreen(
    livestreamApi: LivestreamApi,
    navigator: DestinationsNavigator,
    preferencesManager: PreferencesManager,
) {
    val context = LocalContext.current
    var userName by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(preferencesManager.getUsername()))
    }
    var roomName by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }

    ConstraintLayout(
        modifier = Modifier
            .padding(Dimens.spacer)
            .fillMaxSize()
    ) {
        val (content, joinButton) = createRefs()
        Column(modifier = Modifier.constrainAs(content) {
            width = Dimension.fillToConstraints
            height = Dimension.fillToConstraints
            start.linkTo(parent.start)
            end.linkTo(parent.end)
            top.linkTo(parent.top)
            bottom.linkTo(joinButton.top)
        }) {
            BackButton {
                navigator.navigateUp()
            }

            Text(
                text = "Join Livestream",
                fontWeight = FontWeight.W700,
                fontSize = 34.sp
            )

            Spacer(47.dp)

            OutlinedTextField(
                value = userName,
                onValueChange = { userName = it },
                label = { Text("Your Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(40.dp)

            OutlinedTextField(
                value = roomName,
                onValueChange = { roomName = it },
                label = { Text("Livestream Code") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
        var isCreatingStream by remember { mutableStateOf(false) }
        val coroutineScope = rememberCoroutineScope()

        fun startLoad() {
            isCreatingStream = true
            coroutineScope.launch {
                var response: JoinStreamResponse? = null
                try {
                    response = livestreamApi.joinStream(
                        JoinStreamRequest(
                            roomName = roomName.text,
                            identity = userName.text,
                        )
                    ).body()
                } catch (e: Exception) {
                    Timber.e(e) { "error" }
                }
                if (response != null) {
                    Timber.v { "response received: $response" }
                    navigator.navigate(
                        RoomScreenContainerDestination(
                            apiAuthToken = response.authToken,
                            connectionDetails = response.connectionDetails,
                            isHost = false,
                            initialCameraPosition = CameraPosition.FRONT,
                        )
                    )
                } else {
                    Timber.e { "response failed!" }
                    Toast.makeText(
                        context,
                        "Error occurred while joining the stream. Please check the code and try again.",
                        Toast.LENGTH_LONG
                    ).show()
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
            text = "Join livestream",
            colors = joinButtonColors,
            onClick = {
                preferencesManager.setUsername(userName.text)
                startLoad()
            },
            enabled = userName.text.isNotBlank() && roomName.text.isNotBlank(),
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
