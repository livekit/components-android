/*
 * Copyright 2024 LiveKit, Inc.
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

package io.livekit.android.compose.state

import app.cash.molecule.RecompositionClock
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import io.livekit.android.room.SignalClient
import io.livekit.android.room.participant.RemoteParticipant
import io.livekit.android.room.track.Track
import io.livekit.android.test.MockE2ETest
import io.livekit.android.test.mock.MockMediaStream
import io.livekit.android.test.mock.MockRtpReceiver
import io.livekit.android.test.mock.MockVideoStreamTrack
import io.livekit.android.test.mock.TestData
import io.livekit.android.test.mock.createMediaStreamId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito

@OptIn(ExperimentalCoroutinesApi::class)
class RememberTrackReferencesTest : MockE2ETest() {

    @Test
    fun getEmptyTrackReferences() = runTest {
        connect()
        moleculeFlow(RecompositionClock.Immediate) {
            rememberTracks(passedRoom = room)
        }.test {
            assertTrue(awaitItem().isEmpty())
        }
    }

    @Test
    fun getPlaceholderTrackReferences() = runTest {
        connect()
        moleculeFlow(RecompositionClock.Immediate) {
            rememberTracks(usePlaceholders = setOf(Track.Source.CAMERA), passedRoom = room)
        }.test {
            val trackRefs = awaitItem()
            assertEquals(1, trackRefs.size)
            val trackRef = trackRefs.first()
            assertTrue(trackRef.isPlaceholder())
            assertEquals(Track.Source.CAMERA, trackRef.source)
            assertEquals(room.localParticipant, trackRef.participant)
        }
    }

    @Test
    fun whenParticipantJoinsWithTracks() = runTest {
        connect()
        val job = coroutineRule.scope.launch {
            moleculeFlow(RecompositionClock.Immediate) {
                rememberTracks(passedRoom = room, onlySubscribed = false)
            }.test {
                // discard initial state.
                assertTrue(awaitItem().isEmpty())

                val trackRefs = awaitItem()
                assertEquals(1, trackRefs.size)

                val trackRef = trackRefs.first()
                val remoteParticipant = room.remoteParticipants.values.first()
                val (remoteTrackPublication) = remoteParticipant.videoTrackPublications.first()
                assertFalse(trackRef.isPlaceholder())
                assertEquals(Track.Source.CAMERA, trackRef.source)
                assertEquals(remoteParticipant, trackRef.participant)
                assertEquals(remoteTrackPublication, trackRef.publication)
            }
        }
        simulateMessageFromServer(TestData.PARTICIPANT_JOIN)
        job.join()
    }

    @Test
    fun whenParticipantDisconnects() = runTest {
        connect()
        val job = coroutineRule.scope.launch {
            moleculeFlow(RecompositionClock.Immediate) {
                rememberTracks(passedRoom = room, onlySubscribed = false)
            }.test {
                assertTrue(awaitItem().isEmpty()) // initial
                assertTrue(awaitItem().isNotEmpty()) // join
                assertTrue(awaitItem().isEmpty()) // disconnect
            }
        }
        simulateMessageFromServer(TestData.PARTICIPANT_JOIN)
        simulateMessageFromServer(TestData.PARTICIPANT_DISCONNECT)
        job.join()
    }


    @Test
    fun whenRemoteParticipantTrackSubscribed() = runTest {
        connect()
        val job = coroutineRule.scope.launch {
            moleculeFlow(RecompositionClock.Immediate) {
                rememberTracks(passedRoom = room, onlySubscribed = true)
            }.test {
                // discard initial state.
                assertTrue(awaitItem().isEmpty())

                val trackRefs = awaitItem()
                assertEquals(1, trackRefs.size)

                val remoteParticipant = room.remoteParticipants.values.first()
                val trackRef = trackRefs.first()
                val (remoteTrackPublication) = remoteParticipant.videoTrackPublications.first()
                assertFalse(trackRef.isPlaceholder())
                assertEquals(Track.Source.CAMERA, trackRef.source)
                assertEquals(remoteParticipant, trackRef.participant)
                assertEquals(remoteTrackPublication, trackRef.publication)
            }
        }
        simulateMessageFromServer(TestData.PARTICIPANT_JOIN)
        room.onAddTrack(
            MockRtpReceiver.create(),
            MockVideoStreamTrack(),
            arrayOf(
                MockMediaStream(
                    id = createMediaStreamId(
                        TestData.REMOTE_PARTICIPANT.sid,
                        TestData.REMOTE_VIDEO_TRACK.sid,
                    ),
                ),
            ),
        )
        job.join()
    }

    private fun createFakeRemoteParticipant(): RemoteParticipant {
        return RemoteParticipant(
            TestData.REMOTE_PARTICIPANT,
            Mockito.mock(SignalClient::class.java),
            Dispatchers.IO,
            Dispatchers.Default,
        ).apply {
            updateFromInfo(TestData.REMOTE_PARTICIPANT)
        }
    }
}
