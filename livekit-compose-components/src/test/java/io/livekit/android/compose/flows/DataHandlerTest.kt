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

package io.livekit.android.compose.flows

import androidx.compose.runtime.collectAsState
import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import com.google.protobuf.ByteString
import io.livekit.android.compose.flow.DataSendOptions
import io.livekit.android.compose.flow.rememberDataMessageHandler
import io.livekit.android.compose.test.util.composeTest
import io.livekit.android.room.RTCEngine
import io.livekit.android.room.track.DataPublishReliability
import io.livekit.android.test.MockE2ETest
import io.livekit.android.test.mock.MockDataChannel
import io.livekit.android.test.mock.MockPeerConnection
import kotlinx.coroutines.launch
import livekit.LivekitModels
import livekit.org.webrtc.DataChannel
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Test
import java.nio.ByteBuffer

class DataHandlerTest : MockE2ETest() {
    @Test
    fun sendMessage() = runTest {
        connect()

        val messageString = "message"
        moleculeFlow(RecompositionMode.Immediate) {
            rememberDataMessageHandler(room)
        }.composeTest {
            val dataHandler = awaitItem()
            Assert.assertNotNull(dataHandler)

            dataHandler.sendMessage(messageString.toByteArray(), DataSendOptions(reliability = DataPublishReliability.RELIABLE))

            val pubPeerConnection = component.rtcEngine().getPublisherPeerConnection() as MockPeerConnection
            val dataChannel = pubPeerConnection.dataChannels[RTCEngine.RELIABLE_DATA_CHANNEL_LABEL] as MockDataChannel

            Assert.assertEquals(1, dataChannel.sentBuffers.size)

            val data = dataChannel.sentBuffers.first()!!.data
            val dataPacket = LivekitModels.DataPacket.parseFrom(ByteString.copyFrom(data))
            val sentMessage = dataPacket.user.payload!!
                .toByteArray()
                .decodeToString()
            Assert.assertEquals(messageString, sentMessage)
        }
    }

    @Test
    fun receiveMessage() = runTest {
        connect()

        // Setup subscriber data channels
        val subPeerConnection = component.rtcEngine().getSubscriberPeerConnection() as MockPeerConnection
        val subDataChannel = MockDataChannel(RTCEngine.RELIABLE_DATA_CHANNEL_LABEL)
        subPeerConnection.observer?.onDataChannel(subDataChannel)

        val messageString = "message"
        val job = coroutineRule.scope.launch {
            moleculeFlow(RecompositionMode.Immediate) {
                rememberDataMessageHandler(room).messageFlow.collectAsState(initial = null).value
            }.composeTest {
                // discard initial state.
                awaitItem()

                assertEquals(messageString, awaitItem()!!.payload.decodeToString())
            }
        }

        val dataPacket = with(LivekitModels.DataPacket.newBuilder()) {
            user = with(LivekitModels.UserPacket.newBuilder()) {
                payload = ByteString.copyFrom(messageString, Charsets.UTF_8)
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
