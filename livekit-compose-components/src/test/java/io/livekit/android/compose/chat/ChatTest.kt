package io.livekit.android.compose.chat

import androidx.compose.runtime.collectAsState
import app.cash.molecule.RecompositionClock
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.protobuf.ByteString
import io.livekit.android.compose.flow.DataTopic
import io.livekit.android.room.RTCEngine
import io.livekit.android.test.MockE2ETest
import io.livekit.android.test.mock.MockDataChannel
import io.livekit.android.test.mock.MockPeerConnection
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import livekit.LivekitModels
import livekit.org.webrtc.DataChannel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.nio.ByteBuffer

@OptIn(ExperimentalCoroutinesApi::class)
class ChatTest : MockE2ETest() {
    @Test
    fun sendMessage() = runTest {
        connect()

        val messageString = "message"
        moleculeFlow(RecompositionClock.Immediate) {
            rememberChat(room)
        }.test {
            val chat = awaitItem()
            assertNotNull(chat)

            chat.send(messageString)
            val pubPeerConnection = component.rtcEngine().getPublisherPeerConnection() as MockPeerConnection
            val dataChannel = pubPeerConnection.dataChannels[RTCEngine.RELIABLE_DATA_CHANNEL_LABEL] as MockDataChannel

            assertEquals(1, dataChannel.sentBuffers.size)

            val data = dataChannel.sentBuffers.first()!!.data
            val dataPacket = LivekitModels.DataPacket.parseFrom(ByteString.copyFrom(data))
            val chatMessage = dataPacket.user.payload!!
                .toByteArray()
                .decodeToString()
                .run {
                    val json = Json { ignoreUnknownKeys = true }
                    json.decodeFromString<ChatMessage>(this)
                }

            assertEquals(messageString, chatMessage.message)
        }
    }

    @Test
    fun receiveMessage() = runTest {
        connect()

        // Setup data channels
        val subPeerConnection = component.rtcEngine().getSubscriberPeerConnection() as MockPeerConnection
        val subDataChannel = MockDataChannel(RTCEngine.RELIABLE_DATA_CHANNEL_LABEL)
        subPeerConnection.observer?.onDataChannel(subDataChannel)

        val chatMessage = ChatMessage(timestamp = 0L, message = "message")
        val job = coroutineRule.scope.launch {
            moleculeFlow(RecompositionClock.Immediate) {
                rememberChat(room).messages.value
            }.test {
                // Discard initial state
                awaitItem()
                val receivedMsgs = awaitItem()

                assertEquals(1, receivedMsgs.size)
                assertEquals(chatMessage, receivedMsgs.first())
            }
        }

        val dataPacket = with(LivekitModels.DataPacket.newBuilder()) {
            user = with(LivekitModels.UserPacket.newBuilder()) {
                topic = DataTopic.CHAT.value
                payload = ByteString.copyFrom(Json.encodeToString(chatMessage).toByteArray())
                build()
            }
            build()
        }
        val dataBuffer = DataChannel.Buffer(
            ByteBuffer.wrap(dataPacket.toByteArray()),
            true,
        )

        subDataChannel.observer?.onMessage(dataBuffer)
        job.join()
    }


    @Test
    fun receiveMessageFlow() = runTest {
        connect()

        // Setup data channels
        val subPeerConnection = component.rtcEngine().getSubscriberPeerConnection() as MockPeerConnection
        val subDataChannel = MockDataChannel(RTCEngine.RELIABLE_DATA_CHANNEL_LABEL)
        subPeerConnection.observer?.onDataChannel(subDataChannel)

        val chatMessage = ChatMessage(timestamp = 0L, message = "message")
        val job = coroutineRule.scope.launch {
            moleculeFlow(RecompositionClock.Immediate) {
                rememberChat(room).messagesFlow.collectAsState(initial = null).value
            }.test {
                // Discard initial state
                awaitItem()
                val receivedMsg = awaitItem()

                assertEquals(chatMessage, receivedMsg)
            }
        }

        val dataPacket = with(LivekitModels.DataPacket.newBuilder()) {
            user = with(LivekitModels.UserPacket.newBuilder()) {
                topic = DataTopic.CHAT.value
                payload = ByteString.copyFrom(Json.encodeToString(chatMessage).toByteArray())
                build()
            }
            build()
        }
        val dataBuffer = DataChannel.Buffer(
            ByteBuffer.wrap(dataPacket.toByteArray()),
            true,
        )

        subDataChannel.observer?.onMessage(dataBuffer)
        job.join()
    }
}