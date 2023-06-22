package io.livekit.android.sample.livestream.ui.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import io.livekit.android.compose.local.RoomLocal
import io.livekit.android.compose.local.RoomScope
import io.livekit.android.compose.local.rememberVideoTrack
import io.livekit.android.compose.local.rememberVideoTrackPublication
import io.livekit.android.compose.ui.ScaleType
import io.livekit.android.compose.ui.VideoRenderer
import io.livekit.android.sample.livestream.DebugServerInfo
import io.livekit.android.sample.livestream.state.rememberVideoHostParticipant
import io.livekit.android.sample.livestream.ui.control.ChatWidget
import io.livekit.android.sample.livestream.ui.control.ChatWidgetMessage
import io.livekit.android.sample.livestream.ui.screen.destinations.StreamOptionsScreenDestination

@Destination
@Composable
fun ViewerScreen(
    navigator: DestinationsNavigator
) {

    RoomScope(
        url = DebugServerInfo.URL,
        token = DebugServerInfo.TOKEN,
        audio = false,
        video = false,
    ) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
        ) {
            val (chatBox, hostScreen) = createRefs()

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
            val messages = remember {
                mutableStateListOf(
                    ChatWidgetMessage(
                        "HealthyLifestyle101",
                        "I struggle with procrastination. Any tips to overcome it?"
                    ),
                    ChatWidgetMessage(
                        "FitnessGuru21",
                        "Thanks for joining, WellnessEnthusiast22! Today we'll be discussing tips for staying motivated and productive. Feel free to ask questions too!"
                    ),
                    ChatWidgetMessage(
                        "WellnessEnthusiast22",
                        "Hey there! Just joined the live. What's the topic for today?"
                    ),
                )
            }
            ChatWidget(
                messages = messages,
                onChatSend = {
                    messages.add(
                        index = 0,
                        ChatWidgetMessage(
                            "You",
                            it
                        )
                    )
                },
                onOptionsClick = { navigator.navigate(StreamOptionsScreenDestination()) },
                modifier = Modifier
                    .constrainAs(chatBox) {
                        width = Dimension.fillToConstraints
                        height = Dimension.fillToConstraints
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    }
            )
        }
    }
}