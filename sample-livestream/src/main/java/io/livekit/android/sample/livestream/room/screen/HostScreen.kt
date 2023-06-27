@file:OptIn(ExperimentalMaterialNavigationApi::class, ExperimentalAnimationApi::class)

package io.livekit.android.sample.livestream.room.screen

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.navigation.plusAssign
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.ModalBottomSheetLayout
import com.google.accompanist.navigation.material.rememberBottomSheetNavigator
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.animations.rememberAnimatedNavHostEngine
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.NavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.dependency
import io.livekit.android.compose.chat.rememberChat
import io.livekit.android.compose.local.RoomLocal
import io.livekit.android.compose.local.RoomScope
import io.livekit.android.compose.local.rememberVideoTrack
import io.livekit.android.compose.local.rememberVideoTrackPublication
import io.livekit.android.compose.ui.ScaleType
import io.livekit.android.compose.ui.VideoRenderer
import io.livekit.android.sample.livestream.NavGraphs
import io.livekit.android.sample.livestream.defaultAnimations
import io.livekit.android.sample.livestream.destinations.HostParticipantListScreenDestination
import io.livekit.android.sample.livestream.room.data.LivestreamApi
import io.livekit.android.sample.livestream.room.state.rememberVideoHostParticipant
import io.livekit.android.sample.livestream.room.ui.ChatWidget
import io.livekit.android.sample.livestream.room.ui.ChatWidgetMessage
import io.livekit.android.sample.livestream.room.ui.RoomControls
import kotlinx.coroutines.launch

@NavGraph
annotation class HostNavGraph(
    val start: Boolean = false
)

@Destination
@Composable
fun HostScreenContainer(url: String, token: String, livestreamApi: LivestreamApi) {

    RoomScope(
        url = url,
        token = token,
        audio = true,
        video = true,
    ) {
        val navController = rememberAnimatedNavController()
        val bottomSheetNavigator = rememberBottomSheetNavigator()
        val navHostEngine = rememberAnimatedNavHostEngine(
            rootDefaultAnimations = defaultAnimations,
        )

        navController.navigatorProvider += bottomSheetNavigator
        ModalBottomSheetLayout(
            bottomSheetNavigator = bottomSheetNavigator,
            // other configuration for you bottom sheet screens, like:
            sheetShape = RoundedCornerShape(16.dp),
            sheetBackgroundColor = MaterialTheme.colorScheme.background
        ) {
            DestinationsNavHost(
                navGraph = NavGraphs.host,
                navController = navController,
                engine = navHostEngine,
                dependenciesContainerBuilder = {
                    dependency(RoomLocal.current)
                    dependency(livestreamApi)
                }
            )
        }
    }
}

@HostNavGraph(start = true)
@Destination
@Composable
fun HostScreen(
    livestreamApi: LivestreamApi,
    navigator: DestinationsNavigator
) {

    val chat by rememberChat()
    val scope = rememberCoroutineScope()

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val (chatBox, hostScreen, viewerButton) = createRefs()

        val hostParticipant by rememberVideoHostParticipant()
        val videoTrackPublication by rememberVideoTrackPublication(participant = hostParticipant)
        val videoTrack by rememberVideoTrack(videoPub = videoTrackPublication)

        VideoRenderer(
            room = RoomLocal.current,
            videoTrack = videoTrack,
            scaleType = ScaleType.Fill,
            modifier = Modifier.constrainAs(hostScreen) {
                width = Dimension.matchParent
                height = Dimension.matchParent
            }
        )
        ChatWidget(
            messages = chat.messages.value.map {
                ChatWidgetMessage(
                    it.participant?.identity ?: "",
                    it.message
                )
            },
            onChatSend = {
                scope.launch { chat.send(it) }
            },
            onOptionsClick = { },
            modifier = Modifier
                .constrainAs(chatBox) {
                    width = Dimension.fillToConstraints
                    height = Dimension.percent(0.5f)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom)
                }
        )

        RoomControls(
            showFlipButton = true,
            participantCount = 10,
            aspectType = false,
            onFlipButtonClick = {},
            onAspectButtonClick = {},
            onParticipantButtonClick = { navigator.navigate(HostParticipantListScreenDestination()) },
            modifier = Modifier.constrainAs(viewerButton) {
                width = Dimension.fillToConstraints
                height = Dimension.wrapContent
                start.linkTo(parent.start, margin = 8.dp)
                end.linkTo(parent.end, margin = 8.dp)

                // Room controls have internal padding, so no margin here.
                top.linkTo(parent.top)
            },
        )
    }
}