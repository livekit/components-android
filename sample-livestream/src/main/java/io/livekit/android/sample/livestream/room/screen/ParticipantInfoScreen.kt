package io.livekit.android.sample.livestream.room.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.spec.DestinationStyleBottomSheet
import io.livekit.android.compose.local.RoomLocal
import io.livekit.android.compose.state.rememberParticipantInfo
import io.livekit.android.sample.livestream.room.data.AuthenticatedLivestreamApi
import io.livekit.android.sample.livestream.ui.control.Spacer

@RoomNavGraph
@Destination(style = DestinationStyleBottomSheet::class)
@Composable
fun ColumnScope.ParticipantInfoScreen(
    participantSid: String,
    authedApi: AuthenticatedLivestreamApi,
    navigator: DestinationsNavigator,
    isHost: IsHost
) {
    val room = RoomLocal.current
    val participant = remember(room, participantSid) {
        room.getParticipant(participantSid)
    }

    if (participant == null) {
        navigator.navigateUp()
        return
    }

    val participantInfo = rememberParticipantInfo(participant)

    Canvas(modifier = Modifier.size(108.dp), onDraw = {
        drawCircle(color = nameToColor(participant.name))
    })

    Spacer(8.dp)

    Text(
        text = participantInfo.name ?: "",
        fontWeight = FontWeight.W700,
        fontSize = 14.sp,
    )

    if (isHost.value) {

    }
}
