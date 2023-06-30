package io.livekit.android.sample.livestream.room.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.spec.DestinationStyleBottomSheet
import io.livekit.android.sample.livestream.ui.theme.Dimens
import io.livekit.android.sample.livestream.ui.theme.LKTextStyle
import io.livekit.android.sample.livestream.ui.theme.LightLine

@HostNavGraph
@Destination(style = DestinationStyleBottomSheet::class)
@Composable
fun ColumnScope.HostStreamOptionsScreen(
    navigator: DestinationsNavigator
) {
    StreamOptionsScreen(navigator = navigator)
}

@ViewerNavGraph
@Destination(style = DestinationStyleBottomSheet::class)
@Composable
fun ColumnScope.ViewerStreamOptionsScreen(
    navigator: DestinationsNavigator
) {
    StreamOptionsScreen(navigator = navigator)
}

@Composable
fun ColumnScope.StreamOptionsScreen(
    navigator: DestinationsNavigator
) {

    Text(
        text = "Options",
        style = LKTextStyle.header,
        modifier = Modifier
            .align(Alignment.CenterHorizontally)
            .padding(24.dp)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(LightLine)
    ) {}

    Column(modifier = Modifier.padding(Dimens.spacer)) {

        val leaveButtonColors = ButtonDefaults.buttonColors(
            contentColor = Color.White,
            containerColor = Color(0xFFF91F3C)
        )
        Button(
            colors = leaveButtonColors,
            onClick = { },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimens.buttonHeight)
        ) {
            Text(
                text = "End stream",
                style = LKTextStyle.largeButton,
            )
        }
    }
}