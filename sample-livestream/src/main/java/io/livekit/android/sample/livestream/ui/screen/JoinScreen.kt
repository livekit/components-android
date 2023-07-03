package io.livekit.android.sample.livestream.ui.screen

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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import io.livekit.android.sample.livestream.ui.control.BackButton
import io.livekit.android.sample.livestream.ui.control.LargeTextButton
import io.livekit.android.sample.livestream.ui.control.Spacer
import io.livekit.android.sample.livestream.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Destination
@Composable
fun JoinScreen(
    navigator: DestinationsNavigator
) {
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
                text = "Join Livestream", fontWeight = FontWeight.W700, fontSize = 34.sp
            )

            Spacer(47.dp)

            var userName by rememberSaveable(stateSaver = TextFieldValue.Saver) {
                mutableStateOf(TextFieldValue(""))
            }
            var roomName by rememberSaveable(stateSaver = TextFieldValue.Saver) {
                mutableStateOf(TextFieldValue(""))
            }

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

        }

        val joinButtonColors = ButtonDefaults.buttonColors(
            contentColor = Color.White,
            containerColor = Color(0xFFB11FF9)
        )
        LargeTextButton(
            text = "Join livestream",
            colors = joinButtonColors,
            onClick = {},
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
