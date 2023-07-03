@file:OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialNavigationApi::class)

package io.livekit.android.sample.livestream.room.screen

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.navigation.plusAssign
import com.github.ajalt.timberkt.Timber
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
import io.livekit.android.compose.state.rememberParticipants
import io.livekit.android.sample.livestream.NavGraphs
import io.livekit.android.sample.livestream.defaultAnimations
import io.livekit.android.sample.livestream.destinations.ParticipantListScreenDestination
import io.livekit.android.sample.livestream.room.data.AuthenticatedLivestreamApi
import io.livekit.android.sample.livestream.room.data.ConnectionDetails
import io.livekit.android.sample.livestream.room.data.RoomMetadata
import io.livekit.android.sample.livestream.room.state.rememberHostParticipant
import io.livekit.android.sample.livestream.room.ui.ChatWidget
import io.livekit.android.sample.livestream.room.ui.ChatWidgetMessage
import io.livekit.android.sample.livestream.room.ui.ParticipantGrid
import io.livekit.android.sample.livestream.room.ui.RoomControls
import io.livekit.android.util.flow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Retrofit


@NavGraph
annotation class RoomNavGraph(
    val start: Boolean = false
)

class ParentDestinationsNavigator(delegate: DestinationsNavigator) : DestinationsNavigator by delegate

/**
 * Serializable types are treated as nav args, which we can't pass through to the start route.
 *
 * These data types are just value holders to get around it.
 */
data class IsHost(val value: Boolean)

data class RoomMetadataHolder(val value: RoomMetadata)

@Destination
@Composable
fun RoomScreenContainer(
    apiAuthToken: String,
    connectionDetails: ConnectionDetails,
    roomMetadata: RoomMetadata,
    isHost: Boolean,
    retrofit: Retrofit,
    navigator: DestinationsNavigator,
) {

    val authedApi = remember {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                var request = chain.request()
                request = request.newBuilder().header("Authorization", "Bearer $apiAuthToken").build()

                return@addInterceptor chain.proceed(request)
            }
            .build()
        retrofit.newBuilder()
            .client(okHttpClient)
            .build()
            .create(AuthenticatedLivestreamApi::class.java)
    }

    RoomScope(
        url = connectionDetails.wsUrl,
        token = connectionDetails.token,
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
                navGraph = NavGraphs.room,
                navController = navController,
                engine = navHostEngine,
                dependenciesContainerBuilder = {
                    dependency(ParentDestinationsNavigator(navigator))
                    dependency(authedApi)
                    dependency(IsHost(value = isHost))
                    dependency(RoomMetadataHolder(value = roomMetadata))
                },
            )
        }
    }
}


@RoomNavGraph(start = true)
@Destination
@Composable
fun RoomScreen(
    roomMetadataHolder: RoomMetadataHolder,
    navigator: DestinationsNavigator
) {

    val chat by rememberChat()
    val scope = rememberCoroutineScope()

    val room = RoomLocal.current
    val roomMetadata = room::metadata.flow.collectAsState()
    Timber.e { "Room screen: ${roomMetadata.value}" }
    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val (chatBox, hostScreen, viewerButton) = createRefs()

        val hostParticipant = rememberHostParticipant(roomMetadataHolder.value)
        val videoTrackPublication by rememberVideoTrackPublication(participant = hostParticipant)
        val videoTrack by rememberVideoTrack(videoPub = videoTrackPublication)

        ParticipantGrid(
            videoTracks = listOfNotNull(videoTrack),
            modifier = Modifier
                .constrainAs(hostScreen) {
                    width = Dimension.matchParent
                    height = Dimension.matchParent
                }
        )
        ChatWidget(
            messages = chat.messages.value.map {
                ChatWidgetMessage(
                    it.participant?.name ?: "",
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
            participantCount = rememberParticipants().size,
            aspectType = false,
            onFlipButtonClick = {},
            onAspectButtonClick = {},
            onParticipantButtonClick = { navigator.navigate(ParticipantListScreenDestination()) },
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