/*
 * Copyright 2025 LiveKit, Inc.
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

package io.livekit.android.compose.stream

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import io.livekit.android.compose.test.util.composeTest
import io.livekit.android.compose.test.util.receiveTextStream
import io.livekit.android.room.RTCEngine
import io.livekit.android.test.MockE2ETest
import io.livekit.android.test.mock.MockDataChannel
import io.livekit.android.test.mock.MockPeerConnection
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RememberTextStreamTest : MockE2ETest() {

    @Test
    fun textStreamUpdates() = runTest {
        connect()
        val subPeerConnection = component.rtcEngine().getSubscriberPeerConnection() as MockPeerConnection
        val subDataChannel = MockDataChannel(RTCEngine.RELIABLE_DATA_CHANNEL_LABEL)
        subPeerConnection.observer?.onDataChannel(subDataChannel)

        val job = coroutineRule.scope.launch {
            moleculeFlow(RecompositionMode.Immediate) {
                rememberTextStream("topic", room).value
            }.composeTest {
                run {
                    val initial = awaitItem()
                    println("initial: $initial")
                    assertTrue(initial.isEmpty())

                    println("first")
                    val first = awaitItem()
                    println("first: $first")
                    assertEquals(1, first.size)
                    assertEquals("hello", first[0].text)

                    println("second")
                    val second = awaitItem()
                    println("second: $second")
                    assertEquals(1, second.size)
                    assertEquals("hello world", second[0].text)

                    println("third")
                    val third = awaitItem()
                    println("third: $third")
                    assertEquals(1, third.size)
                    assertEquals("hello world!", third[0].text)
                }
            }
        }

        subDataChannel.observer?.receiveTextStream(chunks = listOf("hello", " world", "!"))

        job.join()
    }

    @Test
    fun multipleTextStreams() = runTest {
        connect()
        val subPeerConnection = component.rtcEngine().getSubscriberPeerConnection() as MockPeerConnection
        val subDataChannel = MockDataChannel(RTCEngine.RELIABLE_DATA_CHANNEL_LABEL)
        subPeerConnection.observer?.onDataChannel(subDataChannel)

        val job = coroutineRule.scope.launch {
            moleculeFlow(RecompositionMode.Immediate) {
                rememberTextStream("topic", room).value
            }.composeTest {
                run {
                    val initial = awaitItem()
                    println("initial: $initial")
                    assertTrue(initial.isEmpty())

                    println("first")
                    val first = awaitItem()
                    println("first: $first")
                    assertEquals(1, first.size)
                    assertEquals("hello", first[0].text)

                    println("second")
                    val second = awaitItem()
                    println("second: $second")
                    assertEquals(2, second.size)
                    assertEquals("hello", second[0].text)
                    assertEquals("world", second[1].text)
                }
            }
        }

        subDataChannel.observer?.receiveTextStream(streamId = "streamId1", chunk = "hello")
        subDataChannel.observer?.receiveTextStream(streamId = "streamId2", chunk = "world")

        job.join()
    }
}
