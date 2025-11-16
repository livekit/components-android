package io.livekit.android.compose.state.transcriptions

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import io.livekit.android.annotations.Beta
import io.livekit.android.compose.flow.DataTopic
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

@OptIn(Beta::class)
class RememberTranscriptionsTest : MockE2ETest() {


    @Test
    fun textStreamUpdates() = runTest {
        connect()
        val subPeerConnection = component.rtcEngine().getSubscriberPeerConnection() as MockPeerConnection
        val subDataChannel = MockDataChannel(RTCEngine.RELIABLE_DATA_CHANNEL_LABEL)
        subPeerConnection.observer?.onDataChannel(subDataChannel)

        val job = coroutineRule.scope.launch {
            moleculeFlow(RecompositionMode.Immediate) {
                rememberTranscriptions(room).value
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

        subDataChannel.observer?.receiveTextStream(chunks = listOf("hello", " world", "!"), topic = DataTopic.TRANSCRIPTION.value)

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
                rememberTranscriptions(room).value
            }.composeTest {
                run {
                    val initial = awaitItem()
                    println("initial: $initial")
                    assertTrue(initial.isEmpty())

                    println("first")
                    val first = awaitItem()
                    println("first: $first")
//                    assertEquals(1, first.size)
//                    assertEquals("hello", first[0].text)

                    println("second")
                    val second = awaitItem()
                    println("second: $second")
//                    assertEquals(2, second.size)
//                    assertEquals("hello", second[0].text)
//                    assertEquals("world", second[1].text)

                    expectNoEvents()

                }
            }
        }

        subDataChannel.observer?.receiveTextStream(streamId = "streamId1", chunk = "hello", topic = DataTopic.TRANSCRIPTION.value)
        subDataChannel.observer?.receiveTextStream(streamId = "streamId2", chunk = "world", topic = DataTopic.TRANSCRIPTION.value)

        job.join()

    }
}