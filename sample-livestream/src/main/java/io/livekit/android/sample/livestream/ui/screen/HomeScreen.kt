package io.livekit.android.sample.livestream.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import io.livekit.android.sample.livestream.ui.screen.destinations.JoinScreenDestination
import io.livekit.android.sample.livestream.ui.screen.destinations.StartScreenDestination
import io.livekit.android.sample.livestream.ui.theme.Dimens

@RootNavGraph(start = true)
@Destination
@Composable
fun HomeScreen(
    navigator: DestinationsNavigator
) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
    ) {

        val (content, startButton, joinButton) = createRefs()

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(43.dp)
                .constrainAs(content) {
                    width = Dimension.fillToConstraints
                    height = Dimension.wrapContent
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    bottom.linkTo(startButton.top)
                }) {

            Surface(
                color = Color.White,
                modifier = Modifier.size(88.dp)
            ) {}

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "Welcome!",
                fontSize = 34.sp,
                fontWeight = FontWeight.W700,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Welcome to the LiveKit live streaming demo app. You can join or start your own stream. Hosted on LiveKit Cloud.",
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        val startButtonColors = ButtonDefaults.buttonColors(
            contentColor = Color.White,
            containerColor = Color(0xFFB11FF9)
        )
        Button(
            colors = startButtonColors,
            onClick = { navigator.navigate(StartScreenDestination()) },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.constrainAs(startButton) {
                width = Dimension.fillToConstraints
                height = Dimension.value(Dimens.buttonHeight)
                bottom.linkTo(joinButton.top, margin = Dimens.spacer)
                start.linkTo(parent.start, margin = Dimens.spacer)
                end.linkTo(parent.end, margin = Dimens.spacer)
            }
        ) {
            Text(
                text = "Start a livestream",
                fontSize = 17.sp,
                fontWeight = FontWeight.W700,
            )
        }

        val joinButtonColors = ButtonDefaults.buttonColors(
            contentColor = Color.White,
            containerColor = Color(0xFF131313)
        )
        Button(
            colors = joinButtonColors,
            onClick = { navigator.navigate(JoinScreenDestination()) },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.constrainAs(joinButton) {
                width = Dimension.fillToConstraints
                height = Dimension.value(Dimens.buttonHeight)
                bottom.linkTo(parent.bottom, margin = Dimens.spacer)
                start.linkTo(parent.start, margin = Dimens.spacer)
                end.linkTo(parent.end, margin = Dimens.spacer)
            }
        ) {
            Text(
                text = "Join a livestream",
                fontSize = 17.sp,
                fontWeight = FontWeight.W700,
            )
        }
    }
}