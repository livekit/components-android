package io.livekit.android.sample.livestream.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.spec.DestinationStyleBottomSheet
import io.livekit.android.room.Room
import io.livekit.android.room.participant.Participant
import io.livekit.android.sample.livestream.room.screen.HostNavGraph
import io.livekit.android.sample.livestream.room.screen.ViewerNavGraph
import io.livekit.android.sample.livestream.ui.control.Spacer
import io.livekit.android.sample.livestream.ui.theme.Dimens
import io.livekit.android.sample.livestream.ui.theme.LKTextStyle
import io.livekit.android.sample.livestream.ui.theme.LightLine
import okhttp3.internal.toHexString
import timber.log.Timber

@HostNavGraph
@Destination(style = DestinationStyleBottomSheet::class)
@Composable
fun HostParticipantListScreen(
    room: Room,
    navigator: DestinationsNavigator
) {
    ParticipantListScreen(room = room, navigator = navigator)
}

@ViewerNavGraph
@Destination(style = DestinationStyleBottomSheet::class)
@Composable
fun ViewerParticipantListScreen(
    room: Room,
    navigator: DestinationsNavigator
) {
    ParticipantListScreen(room = room, navigator = navigator)
}

@Composable
fun ParticipantListScreen(
    room: Room,
    navigator: DestinationsNavigator
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            text = "Viewers",
            style = LKTextStyle.header,
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(LightLine)
    ) {}

    LazyColumn(modifier = Modifier.padding(Dimens.spacer)) {

        item {
            Text(
                text = "Requests to join".uppercase(),
                style = LKTextStyle.listSectionHeader
            )
            Spacer(size = Dimens.spacer)
        }

        items(items = listOf(room.localParticipant)) { participant ->
            ParticipantRow(participant = participant)
        }
        item {
            Text(
                text = "Hosts".uppercase(),
                style = LKTextStyle.listSectionHeader
            )
            Spacer(size = Dimens.spacer)
        }

        items(items = listOf(room.localParticipant)) { participant ->
            ParticipantRow(participant = participant)
        }

        item {
            Text(
                text = "Viewers".uppercase(),
                style = LKTextStyle.listSectionHeader
            )
            Spacer(size = Dimens.spacer)
        }

        items(items = listOf(room.localParticipant)) { participant ->
            ParticipantRow(participant = participant)
        }
    }
}

@Composable
fun LazyItemScope.ParticipantRow(participant: Participant) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        val identity = participant.identity ?: ""
        // Profile icon
        Canvas(modifier = Modifier.size(32.dp), onDraw = {
            drawCircle(color = identityToColor(identity))
        })
        Spacer(size = 12.dp)
        Text(participant.identity ?: "")
    }
    Spacer(size = Dimens.spacer)
}

// Create a color based on the identity name.
fun identityToColor(identity: String): Color {
    Timber.e("identity: ${identity.hashCode().toHexString()}")
    Timber.e("identity color: ${(identity.hashCode().toLong() or 0xFF000000).toHexString()}")

    return Color(identity.hashCode().toLong() or 0xFF000000)
}

fun intToARGB(i: Int): String {
    return Integer.toHexString(i shr 24 and 0xFF) +
            Integer.toHexString(i shr 16 and 0xFF) +
            Integer.toHexString(i shr 8 and 0xFF) +
            Integer.toHexString(i and 0xFF)
}