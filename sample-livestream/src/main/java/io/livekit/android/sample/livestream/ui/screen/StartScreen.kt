package io.livekit.android.sample.livestream.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import io.livekit.android.sample.livestream.destinations.StartPreviewScreenDestination
import io.livekit.android.sample.livestream.ui.control.BackButton
import io.livekit.android.sample.livestream.ui.control.Spacer
import io.livekit.android.sample.livestream.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Destination
@Composable
fun StartScreen(
    navigator: DestinationsNavigator
) {
    ConstraintLayout(
        modifier = Modifier
            .padding(Dimens.spacer)
            .fillMaxSize()
    ) {
        var userName by rememberSaveable(stateSaver = TextFieldValue.Saver) {
            mutableStateOf(TextFieldValue(""))
        }
        var roomName by rememberSaveable(stateSaver = TextFieldValue.Saver) {
            mutableStateOf(TextFieldValue(""))
        }
        var chatEnabled by rememberSaveable {
            mutableStateOf(true)
        }

        var viewerJoinRequestEnabled by rememberSaveable {
            mutableStateOf(true)
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

            Spacer(47.dp)


            OutlinedTextField(
                value = userName,
                onValueChange = { userName = it },
                label = { Text("Your Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(40.dp)

            OutlinedTextField(
                value = roomName,
                onValueChange = { roomName = it },
                label = { Text("Room Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(40.dp)

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


        }

        val joinButtonColors = ButtonDefaults.buttonColors(
            contentColor = Color.White,
            containerColor = Color(0xFFB11FF9)
        )
        Button(
            colors = joinButtonColors,
            onClick = {
                navigator.navigate(
                    StartPreviewScreenDestination(
                        name = userName.text,
                        roomName = roomName.text,
                        enableChat = chatEnabled,
                        allowParticipation = viewerJoinRequestEnabled
                    )
                )
            },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.constrainAs(joinButton) {
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