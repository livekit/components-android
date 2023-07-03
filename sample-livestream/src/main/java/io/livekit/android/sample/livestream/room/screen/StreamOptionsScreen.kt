package io.livekit.android.sample.livestream.room.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.spec.DestinationStyleBottomSheet
import io.livekit.android.sample.livestream.room.data.AuthenticatedLivestreamApi
import io.livekit.android.sample.livestream.ui.control.LargeTextButton
import io.livekit.android.sample.livestream.ui.theme.Dimens
import io.livekit.android.sample.livestream.ui.theme.LKTextStyle
import io.livekit.android.sample.livestream.ui.theme.LightLine
import kotlinx.coroutines.launch

@RoomNavGraph
@Destination(style = DestinationStyleBottomSheet::class)
@Composable
fun ColumnScope.StreamOptionsScreen(
    authedApi: AuthenticatedLivestreamApi,
    parentNavigator: ParentDestinationsNavigator,
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

        val coroutineScope = rememberCoroutineScope()
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
    }
}