/*
 * Copyright 2024-2025 LiveKit, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.livekit.android.compose.chat

import androidx.compose.runtime.collectAsState
import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import com.google.protobuf.ByteString
import io.livekit.android.compose.flow.DataTopic
import io.livekit.android.compose.flow.LegacyDataTopic
import io.livekit.android.compose.test.util.composeTest
import io.livekit.android.compose.test.util.receiveTextStream
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
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.ByteBuffer

@OptIn(ExperimentalCoroutinesApi::class)
class ChatTest : MockE2ETest() {
    @Test
    fun sendMessage() = runTest {
        connect()

        val messageString = "message"
        moleculeFlow(RecompositionMode.Immediate) {
            rememberChat(room)
        }.composeTest {
            val chat = awaitItem()
            assertNotNull(chat)

            val result = chat.send(messageString)
            assertTrue(result.isSuccess)
            val pubPeerConnection = component.rtcEngine().getPublisherPeerConnection() as MockPeerConnection
            val dataChannel = pubPeerConnection.dataChannels[RTCEngine.RELIABLE_DATA_CHANNEL_LABEL] as MockDataChannel

            assertEquals(4, dataChannel.sentBuffers.size)

            // Data stream send
            run {
                val data = dataChannel.sentBuffers[1].data
                val dataPacket = LivekitModels.DataPacket.parseFrom(ByteString.copyFrom(data))
                val chatMessage = dataPacket.streamChunk.content
                    .toStringUtf8()

                assertEquals(messageString, chatMessage)
            }
            // Legacy chat send
            run {
                val data = dataChannel.sentBuffers[3].data
                val dataPacket = LivekitModels.DataPacket.parseFrom(ByteString.copyFrom(data))
                val chatMessage = dataPacket.user.payload!!
                    .toByteArray()
                    .decodeToString()
                    .run {
                        val json = Json { ignoreUnknownKeys = true }
                        json.decodeFromString<LegacyChatMessage>(this)
                    }

                assertEquals(messageString, chatMessage.message)
            }
        }
    }

    @Test
    fun receiveDataStreamMessage() = runTest {
        connect()

        // Setup data channels
        val subPeerConnection = component.rtcEngine().getSubscriberPeerConnection() as MockPeerConnection
        val subDataChannel = MockDataChannel(RTCEngine.RELIABLE_DATA_CHANNEL_LABEL)
        subPeerConnection.observer?.onDataChannel(subDataChannel)

        val job = coroutineRule.scope.launch {
            moleculeFlow(RecompositionMode.Immediate) {
                rememberChat(room).messages.value
            }.composeTest {
                // Discard initial state
                val first = awaitItem()
                println("first: $first")
                val receivedMsgs = awaitItem()
                println("receivedMsgs: $receivedMsgs")

                assertEquals(1, receivedMsgs.size)
                assertEquals("message", receivedMsgs.first().message)
            }
        }

        subDataChannel.observer?.receiveTextStream(chunk = "message", topic = DataTopic.CHAT.value)
        job.join()
    }

    @Test
    fun receiveLegacyMessage() = runTest {
        connect()

        // Setup data channels
        val subPeerConnection = component.rtcEngine().getSubscriberPeerConnection() as MockPeerConnection
        val subDataChannel = MockDataChannel(RTCEngine.RELIABLE_DATA_CHANNEL_LABEL)
        subPeerConnection.observer?.onDataChannel(subDataChannel)

        val chatMessage = LegacyChatMessage(timestamp = 0L, message = "message")
        val job = coroutineRule.scope.launch {
            moleculeFlow(RecompositionMode.Immediate) {
                rememberChat(room).messages.value
            }.composeTest {
                // Discard initial state
                awaitItem()
                val receivedMsgs = awaitItem()

                assertEquals(1, receivedMsgs.size)
                assertEquals(chatMessage.message, receivedMsgs.first().message)
            }
        }

        val dataPacket = with(LivekitModels.DataPacket.newBuilder()) {
            user = with(LivekitModels.UserPacket.newBuilder()) {
                topic = LegacyDataTopic.CHAT.value
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
    fun receiveLegacyMessageFlow() = runTest {
        connect()

        // Setup data channels
        val subPeerConnection = component.rtcEngine().getSubscriberPeerConnection() as MockPeerConnection
        val subDataChannel = MockDataChannel(RTCEngine.RELIABLE_DATA_CHANNEL_LABEL)
        subPeerConnection.observer?.onDataChannel(subDataChannel)

        val chatMessage = LegacyChatMessage(timestamp = 0L, message = "message")
        val job = coroutineRule.scope.launch {
            moleculeFlow(RecompositionMode.Immediate) {
                rememberChat(room).messagesFlow.collectAsState(initial = null).value
            }.composeTest {
                // Discard initial state
                awaitItem()
                val receivedMsg = awaitItem()

                assertEquals(chatMessage.message, receivedMsg?.message)
            }
        }

        val dataPacket = with(LivekitModels.DataPacket.newBuilder()) {
            user = with(LivekitModels.UserPacket.newBuilder()) {
                topic = LegacyDataTopic.CHAT.value
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
