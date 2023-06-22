package io.livekit.android.sample.livestream.ui.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.ramcosta.composedestinations.annotation.Destination
import io.livekit.android.compose.chat.rememberChat
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
import kotlinx.coroutines.launch

@Destination
@Composable
fun HostScreen() {

    RoomScope(
        url = DebugServerInfo.URL,
        token = DebugServerInfo.TOKEN,
        audio = true,
        video = true,
    ) {

        val chat by rememberChat()
        val scope = rememberCoroutineScope()

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
                modifier = Modifier
                    .constrainAs(chatBox) {
                        width = Dimension.fillToConstraints
                        height = Dimension.percent(0.5f)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        bottom.linkTo(parent.bottom)
                    }
            )
        }
    }
}