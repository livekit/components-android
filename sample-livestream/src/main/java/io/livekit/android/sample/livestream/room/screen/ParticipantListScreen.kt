package io.livekit.android.sample.livestream.room.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.spec.DestinationStyleBottomSheet
import io.livekit.android.compose.state.rememberParticipants
import io.livekit.android.sample.livestream.destinations.ParticipantInfoScreenDestination
import io.livekit.android.sample.livestream.room.data.AuthenticatedLivestreamApi
import io.livekit.android.sample.livestream.room.data.IdentityRequest
import io.livekit.android.sample.livestream.room.state.rememberHostParticipant
import io.livekit.android.sample.livestream.room.state.rememberParticipantMetadatas
import io.livekit.android.sample.livestream.room.ui.AvatarIcon
import io.livekit.android.sample.livestream.ui.control.HorizontalLine
import io.livekit.android.sample.livestream.ui.control.SmallTextButton
import io.livekit.android.sample.livestream.ui.control.Spacer
import io.livekit.android.sample.livestream.ui.theme.AppTheme
import io.livekit.android.sample.livestream.ui.theme.Dimens
import io.livekit.android.sample.livestream.ui.theme.LKButtonColors
import io.livekit.android.sample.livestream.ui.theme.LKTextStyle
import kotlinx.coroutines.launch

private const val headerRequestKey = "header_request_key"
private const val headerHostKey = "header_host_key"
private const val headerViewerKey = "header_viewer_key"

/**
 * A BottomSheet screen that shows all the participants in the room.
 */
@OptIn(ExperimentalFoundationApi::class)
@RoomNavGraph
@Destination(style = DestinationStyleBottomSheet::class)
@Composable
fun ParticipantListScreen(
    isHost: IsHost,
    authedApi: AuthenticatedLivestreamApi,
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
        val hostParticipant = rememberHostParticipant(roomMetadataHolder.value.creatorIdentity)

        val hosts = metadatas
            .filter { (participant, metadata) -> metadata.isOnStage || participant == hostParticipant }
            .entries
            .toList()
            .sortedBy { it.key.identity ?: "" }

        // Only visible to the host.
        val requestsToJoin = if (isHost.value) {
            metadatas
                .filter { (participant, metadata) -> metadata.handRaised && !metadata.invitedToStage && !hosts.any { it.key == participant } }
                .entries
                .toList()
                .sortedBy { it.key.identity ?: "" }
        } else {
            emptyList()
        }

        val viewers = metadatas
            .filter { p -> !requestsToJoin.any { it == p } }
            .filter { p -> !hosts.any { it == p } }
            .entries
            .toList()
            .sortedBy { it.key.identity ?: "" }

        LazyColumn {

            if (requestsToJoin.isNotEmpty()) {
                item(headerRequestKey) {
                    Text(
                        text = "Requests to join".uppercase(),
                        style = LKTextStyle.listSectionHeader,
                        modifier = Modifier.animateItemPlacement(),
                    )
                    Spacer(size = Dimens.spacer)
                }

                items(
                    items = requestsToJoin,
                    key = { it.key.sid }
                ) { (participant, metadata) ->
                    ParticipantRow(
                        name = participant.identity ?: "",
                        imageUrl = metadata.avatarImageUrl,
                        isRequestingToJoin = true,
                        onAllowClick = {
                            authedApi.inviteToStage(IdentityRequest(participant.identity ?: ""))
                        },
                        onDenyClick = {
                            authedApi.removeFromStage(IdentityRequest(participant.identity ?: ""))
                        },
                        modifier = Modifier
                            .clickable { navigator.navigate(ParticipantInfoScreenDestination(participant.sid)) }
                            .animateItemPlacement()
                    )
                }
            }

            if (hosts.isNotEmpty()) {
                item(headerHostKey) {
                    Text(
                        text = "Hosts".uppercase(),
                        style = LKTextStyle.listSectionHeader,
                        modifier = Modifier.animateItemPlacement(),
                    )
                    Spacer(size = Dimens.spacer)
                }

                items(
                    items = hosts,
                    key = { it.key.sid }
                ) { (participant, metadata) ->
                    ParticipantRow(
                        name = participant.identity ?: "",
                        imageUrl = metadata.avatarImageUrl,
                        modifier = Modifier
                            .clickable { navigator.navigate(ParticipantInfoScreenDestination(participant.sid)) }
                            .animateItemPlacement()
                    )
                }
            }

            if (viewers.isNotEmpty()) {
                item(headerViewerKey) {
                    Text(
                        text = "Viewers".uppercase(),
                        style = LKTextStyle.listSectionHeader,
                        modifier = Modifier.animateItemPlacement(),
                    )
                    Spacer(size = Dimens.spacer)
                }

                items(
                    items = viewers,
                    key = { it.key.sid }
                ) { (participant, metadata) ->
                    ParticipantRow(
                        name = participant.identity ?: "",
                        imageUrl = metadata.avatarImageUrl,
                        modifier = Modifier
                            .clickable { navigator.navigate(ParticipantInfoScreenDestination(participant.sid)) }
                            .animateItemPlacement()
                    )
                }
            }
        }

    }
}

@Composable
private fun LazyItemScope.ParticipantRow(
    name: String,
    imageUrl: String,
    modifier: Modifier = Modifier,
    isRequestingToJoin: Boolean = false,
    onAllowClick: suspend () -> Unit = {},
    onDenyClick: suspend () -> Unit = {},
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier)
    ) {
        // Profile icon
        AvatarIcon(
            name = name,
            imageUrl = imageUrl,
            modifier = Modifier.size(32.dp)
        )
        Spacer(size = 12.dp)
        Text(name, modifier = Modifier.weight(1f))

        if (isRequestingToJoin) {
            val coroutineScope = rememberCoroutineScope()

            SmallTextButton(
                text = "Allow",
                onClick = {
                    coroutineScope.launch {
                        onAllowClick()
                    }
                },
                colors = LKButtonColors.blueButtonColors(),
                modifier = Modifier.defaultMinSize(60.dp, 30.dp)
            )
            Spacer(8.dp)
            SmallTextButton(
                text = "Deny",
                onClick = {
                    coroutineScope.launch {
                        onDenyClick()
                    }
                },
                colors = LKButtonColors.secondaryButtonColors(),
                modifier = Modifier.defaultMinSize(60.dp, 30.dp)
            )
        }
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

@Preview(showBackground = true)
@Composable
fun ParticipantRowPreview() {

    AppTheme {
        LazyColumn {
            item {
                ParticipantRow(name = "Viewer", imageUrl = "")
            }
            item {
                ParticipantRow(name = "Viewer requesting to Join", imageUrl = "", isRequestingToJoin = true)
            }
        }
    }
}