package io.livekit.android.sample.livestream.room.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.github.ajalt.timberkt.Timber
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.spec.DestinationStyleBottomSheet
import io.livekit.android.compose.state.rememberParticipants
import io.livekit.android.room.participant.Participant
import io.livekit.android.sample.livestream.destinations.ParticipantInfoScreenDestination
import io.livekit.android.sample.livestream.room.state.rememberHostParticipant
import io.livekit.android.sample.livestream.room.state.rememberParticipantMetadatas
import io.livekit.android.sample.livestream.ui.control.HorizontalLine
import io.livekit.android.sample.livestream.ui.control.Spacer
import io.livekit.android.sample.livestream.ui.theme.Dimens
import io.livekit.android.sample.livestream.ui.theme.LKTextStyle

/**
 * A BottomSheet screen that shows all the participants in the room.
 */
@RoomNavGraph
@Destination(style = DestinationStyleBottomSheet::class)
@Composable
fun ParticipantListScreen(
    isHost: IsHost,
    roomMetadataHolder: RoomMetadataHolder,
    navigator: DestinationsNavigator
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(Dimens.spacer),
    ) {
        Text(
            text = "Viewers",
            style = LKTextStyle.header,
        )

        Spacer(Dimens.spacer)
        HorizontalLine()
        Spacer(Dimens.spacer)

        val participants = rememberParticipants()
        val metadatas = rememberParticipantMetadatas()
        val hostParticipant = rememberHostParticipant(roomMetadata = roomMetadataHolder.value)

        val hosts = metadatas
            .filter { (participant, metadata) -> metadata.isOnStage || participant == hostParticipant }
            .map { (participant, _) -> participant }
        val requestsToJoin = metadatas
            .filter { (participant, metadata) -> metadata.handRaised && !metadata.invitedToStage && !hosts.contains(participant) }
            .map { (participant, _) -> participant }

        val viewers = participants
            .filter { p -> !requestsToJoin.contains(p) }
            .filter { p -> !hosts.contains(p) }

        LazyColumn {

            if (requestsToJoin.isNotEmpty()) {
                item {
                    Text(
                        text = "Requests to join".uppercase(),
                        style = LKTextStyle.listSectionHeader
                    )
                    Spacer(size = Dimens.spacer)
                }

                items(
                    items = requestsToJoin,
                    key = { it.sid }
                ) { participant ->
                    ParticipantRow(
                        participant = participant,
                        modifier = Modifier
                            .clickable { navigator.navigate(ParticipantInfoScreenDestination(participant.sid)) }
                    )
                }
            }

            if (hosts.isNotEmpty()) {
                item {
                    Text(
                        text = "Hosts".uppercase(),
                        style = LKTextStyle.listSectionHeader
                    )
                    Spacer(size = Dimens.spacer)
                }

                items(
                    items = hosts,
                    key = { it.sid }
                ) { participant ->
                    ParticipantRow(participant = participant)
                }
            }

            if (viewers.isNotEmpty()) {
                item {
                    Text(
                        text = "Viewers".uppercase(),
                        style = LKTextStyle.listSectionHeader
                    )
                    Spacer(size = Dimens.spacer)
                }

                items(
                    items = viewers,
                    key = { it.sid }
                ) { participant ->
                    ParticipantRow(
                        participant = participant,
                        modifier = Modifier
                            .clickable { navigator.navigate(ParticipantInfoScreenDestination(participant.sid)) }
                    )
                }
            }
        }

    }
}

@Composable
private fun LazyItemScope.ParticipantRow(
    participant: Participant,
    modifier: Modifier = Modifier
) {

    Timber.e { "participant row ${participant.identity ?: ""}" }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier)
    ) {
        val name = participant.identity ?: ""
        // Profile icon
        Canvas(modifier = Modifier.size(32.dp), onDraw = {
            drawCircle(color = nameToColor(name))
        })
        Spacer(size = 12.dp)
        Text(participant.identity ?: "")
    }
    Spacer(size = Dimens.spacer)
}

// Generate a color based on the name.
fun nameToColor(name: String?): Color {
    if (name.isNullOrEmpty()) {
        return Color.White
    }
    return Color(name.hashCode().toLong() or 0xFF000000)
}