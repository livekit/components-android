@file:OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialNavigationApi::class)

package io.livekit.android.sample.livestream.room.screen

import android.widget.Toast
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import io.livekit.android.compose.state.rememberParticipants
import io.livekit.android.sample.livestream.NavGraphs
import io.livekit.android.sample.livestream.defaultAnimations
import io.livekit.android.sample.livestream.destinations.JoinScreenDestination
import io.livekit.android.sample.livestream.destinations.ParticipantListScreenDestination
import io.livekit.android.sample.livestream.destinations.StartPreviewScreenDestination
import io.livekit.android.sample.livestream.destinations.StreamOptionsScreenDestination
import io.livekit.android.sample.livestream.room.data.AuthenticatedLivestreamApi
import io.livekit.android.sample.livestream.room.data.ConnectionDetails
import io.livekit.android.sample.livestream.room.data.RoomMetadata
import io.livekit.android.sample.livestream.room.state.rememberHostParticipant
import io.livekit.android.sample.livestream.room.state.rememberOnStageParticipants
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


/**
 * A container for [RoomScreen] that sets up the needed nav host and dependencies.
 */
@Destination
@Composable
fun RoomScreenContainer(
    okHttpClient: OkHttpClient,
    apiAuthToken: String,
    connectionDetails: ConnectionDetails,
    roomMetadata: RoomMetadata,
    isHost: Boolean,
    retrofit: Retrofit,
    navigator: DestinationsNavigator,
) {

    val authedApi = remember {
        val authedClient = okHttpClient.newBuilder()
            .addInterceptor { chain ->
                var request = chain.request()
                request = request.newBuilder().header("Authorization", "Token $apiAuthToken").build()

                return@addInterceptor chain.proceed(request)
            }
            .build()
        retrofit.newBuilder()
            .client(authedClient)
            .build()
            .create(AuthenticatedLivestreamApi::class.java)
    }

    val roomCoroutineScope = rememberCoroutineScope()

    var enableAudio by remember { mutableStateOf(isHost) }
    var enableVideo by remember { mutableStateOf(isHost) }

    val context = LocalContext.current
    RoomScope(
        url = connectionDetails.wsUrl,
        token = connectionDetails.token,
        audio = enableAudio,
        video = enableVideo,
        onDisconnected = {
            Toast.makeText(context, "Disconnected from livestream.", Toast.LENGTH_LONG).show()
            val route = if (isHost) {
                StartPreviewScreenDestination.route
            } else {
                JoinScreenDestination.route
            }
            navigator.popBackStack(route, false)
        }
    ) {

        // Publish video if have permissions as viewer.
        if (!isHost) {
            val room = RoomLocal.current
            LaunchedEffect(room) {
                room.localParticipant::permissions.flow.collect { permissions ->
                    val canPublish = permissions?.canPublish ?: false
                    enableAudio = canPublish
                    enableVideo = canPublish
                }
            }
        }

        // Setup nav host for RoomScreen
        val navController = rememberAnimatedNavController()
        val bottomSheetNavigator = rememberBottomSheetNavigator()
        val navHostEngine = rememberAnimatedNavHostEngine(
            rootDefaultAnimations = defaultAnimations,
        )

        navController.navigatorProvider += bottomSheetNavigator
        ModalBottomSheetLayout(
            bottomSheetNavigator = bottomSheetNavigator,
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
                    dependency(roomCoroutineScope)
                },
            )
        }
    }
}

/**
 * The room screen, for both hosts and participants to view the stream.
 */
@RoomNavGraph(start = true)
@Destination
@Composable
fun RoomScreen(
    roomMetadataHolder: RoomMetadataHolder,
    navigator: DestinationsNavigator
) {

    val chat by rememberChat()
    val scope = rememberCoroutineScope()

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val (chatBox, hostScreen, viewerButton) = createRefs()

        val hostParticipant = rememberHostParticipant(roomMetadataHolder.value.creatorIdentity)
        val videoParticipants = rememberOnStageParticipants(roomMetadataHolder.value.creatorIdentity)
        val participants = listOf(hostParticipant).plus(videoParticipants)
        val videoTrackPublications = participants.map { rememberVideoTrackPublication(participant = it) }
        val videoTracks = videoTrackPublications.map { rememberVideoTrack(videoPub = it) }

        ParticipantGrid(
            videoTracks = videoTracks,
            modifier = Modifier
                .constrainAs(hostScreen) {
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
            onOptionsClick = { navigator.navigate(StreamOptionsScreenDestination()) },
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