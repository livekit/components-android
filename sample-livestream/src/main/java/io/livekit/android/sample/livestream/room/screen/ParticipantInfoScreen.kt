package io.livekit.android.sample.livestream.room.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
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
import io.livekit.android.sample.livestream.room.data.IdentityRequest
import io.livekit.android.sample.livestream.room.state.rememberParticipantMetadata
import io.livekit.android.sample.livestream.room.ui.AvatarIcon
import io.livekit.android.sample.livestream.ui.control.HorizontalLine
import io.livekit.android.sample.livestream.ui.control.LargeTextButton
import io.livekit.android.sample.livestream.ui.control.Spacer
import io.livekit.android.sample.livestream.ui.theme.Dimens
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * A BottomSheet screen that can show information on a participant as well as moderation controls if [isHost].
 */
@RoomNavGraph
@Destination(style = DestinationStyleBottomSheet::class)
@Composable
fun ParticipantInfoScreen(
    participantSid: String,
    roomMetadataHolder: RoomMetadataHolder,
    isHost: IsHost,
    authedApi: AuthenticatedLivestreamApi,
    coroutineScope: CoroutineScope,
    navigator: DestinationsNavigator,
) {
    val room = RoomLocal.current
    val participant = remember(room, participantSid) {
        room.getParticipant(participantSid)
    }

    if (participant == null) {
        navigator.navigateUp()
        return
    }

    val roomMetadata = roomMetadataHolder.value
    val participantInfo = rememberParticipantInfo(participant)
    val participantMetadata = rememberParticipantMetadata(participant)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimens.spacer)
    ) {

        Text(
            text = "Moderation",
            fontWeight = FontWeight.W700,
            fontSize = 20.sp,
        )

        Spacer(Dimens.spacer)
        HorizontalLine()
        Spacer(Dimens.spacer)

        AvatarIcon(
            imageUrl = participantMetadata.avatarImageUrl,
            name = participant.identity,
            modifier = Modifier
                .size(108.dp)
                .align(Alignment.CenterHorizontally)
        )

        Spacer(8.dp)

        Text(
            text = participantInfo.identity ?: "",
            fontWeight = FontWeight.W700,
            fontSize = 14.sp,
        )

        Spacer(Dimens.spacer)
        HorizontalLine()
        Spacer(Dimens.spacer)

        val identity = participant.identity
        if (isHost.value &&
            identity != roomMetadata.creatorIdentity &&
            identity != null
        ) {
            if (participantMetadata.isOnStage) {
                LargeTextButton(
                    text = "Remove from stage",
                    onClick = {
                        coroutineScope.launch {
                            authedApi.removeFromStage(IdentityRequest(identity))
                        }
                    }
                )
            } else {
                LargeTextButton(
                    text = "Invite to stage",
                    enabled = !participantMetadata.invitedToStage,
                    onClick = {
                        coroutineScope.launch {
                            authedApi.inviteToStage(IdentityRequest(identity))
                        }
                    }
                )
            }

            Spacer(8.dp)

            LargeTextButton(
                text = "Remove from stream",
                onClick = {
                    coroutineScope.launch {
                        TODO("No API available")
                    }
                }
            )
        }
    }
}
