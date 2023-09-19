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

package io.livekit.android.sample.livestream.room.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.spec.DestinationStyleBottomSheet
import io.livekit.android.compose.local.RoomLocal
import io.livekit.android.compose.state.rememberRoomInfo
import io.livekit.android.sample.livestream.room.data.AuthenticatedLivestreamApi
import io.livekit.android.sample.livestream.room.data.IdentityRequest
import io.livekit.android.sample.livestream.room.state.rememberParticipantMetadata
import io.livekit.android.sample.livestream.room.state.rememberRoomMetadata
import io.livekit.android.sample.livestream.ui.control.HorizontalLine
import io.livekit.android.sample.livestream.ui.control.LKTextField
import io.livekit.android.sample.livestream.ui.control.LargeTextButton
import io.livekit.android.sample.livestream.ui.control.Spacer
import io.livekit.android.sample.livestream.ui.theme.Blue500
import io.livekit.android.sample.livestream.ui.theme.Dimens
import io.livekit.android.sample.livestream.ui.theme.LKTextStyle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Displays options relevant to the current livestream.
 */
@RoomNavGraph
@Destination(style = DestinationStyleBottomSheet::class)
@Composable
fun StreamOptionsScreen(
    coroutineScope: CoroutineScope,
    isHost: IsHost,
    authedApi: AuthenticatedLivestreamApi,
    parentNavigator: ParentDestinationsNavigator,
) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        val room = RoomLocal.current
        val roomInfo = rememberRoomInfo()
        val roomMetadata by rememberRoomMetadata()

        val localParticipant = room.localParticipant
        val metadata = rememberParticipantMetadata(participant = localParticipant)

        Text(
            text = "Options",
            style = LKTextStyle.header,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(24.dp)
        )

        HorizontalLine()

        Spacer(Dimens.spacer)
        Text(
            text = "Share Stream Code".uppercase(),
            style = LKTextStyle.listSectionHeader,
            color = Color.White
        )
        Spacer(8.dp)
        TextWithCopyButton(text = roomInfo.name.value ?: "")

        Spacer(Dimens.spacer)
        HorizontalLine()
        Spacer(Dimens.spacer)

        if (isHost.value) {
            val leaveButtonColors = ButtonDefaults.buttonColors(
                contentColor = Color.White,
                containerColor = Color(0xFFF91F3C)
            )
            LargeTextButton(
                text = "End stream",
                colors = leaveButtonColors,
                onClick = {
                    coroutineScope.launch {
                        authedApi.stopStream()
                        parentNavigator.navigateUp()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
            )
        } else if (metadata.isOnStage) {
            LargeTextButton(
                text = "Leave Stage",
                onClick = {
                    coroutineScope.launch {
                        val identity = localParticipant.identity
                        if (identity != null) {
                            authedApi.removeFromStage(IdentityRequest(identity))
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
            )
        } else {
            val text = if (roomMetadata.allowParticipation) {
                "Raise Hand"
            } else {
                "Raise Hand Disabled"
            }
            LargeTextButton(
                text = text,
                onClick = {
                    coroutineScope.launch {
                        authedApi.requestToJoin()
                    }
                },
                enabled = roomMetadata.allowParticipation,
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextWithCopyButton(text: String) {
    val clipboardManager = LocalClipboardManager.current
    Row {
        LKTextField(
            value = text,
            onValueChange = { },
            shape = RoundedCornerShape(5.dp),
            colors = TextFieldDefaults.textFieldColors(
                textColor = Color.White,
                disabledTextColor = Color.Transparent,
                containerColor = Color(0xFF222222),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            placeholder = {
                Text("Type your message...")
            },
            modifier = Modifier
                .weight(1f)
                .height(42.dp)
        )

        Spacer(Dimens.spacer)

        val sendButtonColors = ButtonDefaults.buttonColors(
            containerColor = Blue500,
            contentColor = Color.White
        )

        Button(
            colors = sendButtonColors,
            shape = RoundedCornerShape(5.dp),
            onClick = {
                clipboardManager.setText(AnnotatedString(text))
            },
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier
                .padding(0.dp)
                .height(42.dp)
        ) {
            Text(text = "Copy", modifier = Modifier.padding(0.dp))
        }
    }
}

@Preview
@Composable
fun TextWithCopyButtonPreview() {
    TextWithCopyButton(text = "Preview text ipsum lorem")
}
