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

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import io.livekit.android.room.SignalClient
import io.livekit.android.room.participant.RemoteParticipant
import io.livekit.android.room.participant.VideoTrackPublishOptions
import io.livekit.android.room.track.LocalTrackPublication
import io.livekit.android.room.track.LocalVideoTrack
import io.livekit.android.room.track.Track
import io.livekit.android.room.track.TrackPublication
import io.livekit.android.test.MockE2ETest
import io.livekit.android.test.mock.MockRtpReceiver
import io.livekit.android.test.mock.MockVideoStreamTrack
import io.livekit.android.test.mock.TestData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito

@OptIn(ExperimentalCoroutinesApi::class)
class RememberParticipantTrackReferences : MockE2ETest() {

    @Test
    fun getEmptyTrackReferences() = runTest {
        connect()
        moleculeFlow(RecompositionMode.Immediate) {
            rememberParticipantTrackReferences(passedParticipant = room.localParticipant)
        }.test {
            Assert.assertTrue(awaitItem().isEmpty())
        }
    }

    @Test
    fun getPlaceholderTrackReferences() = runTest {
        connect()
        moleculeFlow(RecompositionMode.Immediate) {
            rememberParticipantTrackReferences(
                usePlaceholders = setOf(Track.Source.CAMERA),
                passedParticipant = room.localParticipant
            )
        }.test {
            val trackRefs = awaitItem()
            Assert.assertEquals(1, trackRefs.size)
            val trackRef = trackRefs.first()
            Assert.assertTrue(trackRef.isPlaceholder())
            Assert.assertEquals(Track.Source.CAMERA, trackRef.source)
            Assert.assertEquals(room.localParticipant, trackRef.participant)
        }
    }

    @Test
    fun whenPublishingTrack() = runTest {
        connect()
        val job = coroutineRule.scope.launch {
            moleculeFlow(RecompositionMode.Immediate) {
                rememberParticipantTrackReferences(
                    passedParticipant = room.localParticipant,
                    onlySubscribed = false
                )
            }.test {
                // discard initial state.
                Assert.assertTrue(awaitItem().isEmpty())

                val trackRefs = awaitItem()
                Assert.assertEquals(1, trackRefs.size)

                val trackRef = trackRefs.first()
                val (trackPub) = room.localParticipant.videoTrackPublications.first()
                Assert.assertFalse(trackRef.isPlaceholder())
                Assert.assertEquals(Track.Source.CAMERA, trackRef.source)
                Assert.assertEquals(room.localParticipant, trackRef.participant)
                Assert.assertEquals(trackPub, trackRef.publication)
            }
        }

        val trackPublication = TrackPublication(TestData.LOCAL_VIDEO_TRACK, null, room.localParticipant)
        room.localParticipant.addTrackPublication(trackPublication)

        job.join()
    }

    @Test
    fun whenParticipantDisconnects() = runTest {
        connect()
        val job = coroutineRule.scope.launch {
            moleculeFlow(RecompositionMode.Immediate) {
                rememberParticipantTrackReferences(passedParticipant = room.localParticipant)
            }.test {
                Assert.assertTrue(awaitItem().isEmpty()) // initial
                Assert.assertTrue(awaitItem().isNotEmpty()) // add
                Assert.assertTrue(awaitItem().isEmpty()) // disconnect
            }
        }
        val mockVideoTrack = Mockito.mock(LocalVideoTrack::class.java)
        val trackPublication =
            LocalTrackPublication(TestData.LOCAL_VIDEO_TRACK, mockVideoTrack, room.localParticipant, VideoTrackPublishOptions())
        room.localParticipant.addTrackPublication(trackPublication)
        room.disconnect()
        job.join()
    }

    @Test
    fun whenRemoteParticipantTrackSubscribed() = runTest {
        val remoteParticipant = createFakeRemoteParticipant()
        val job = coroutineRule.scope.launch {
            moleculeFlow(RecompositionMode.Immediate) {
                rememberParticipantTrackReferences(
                    passedParticipant = remoteParticipant,
                    onlySubscribed = true
                )
            }.test {
                // discard initial state.
                Assert.assertTrue(awaitItem().isEmpty())

                val trackRefs = awaitItem()
                Assert.assertEquals(1, trackRefs.size)
                val trackRef = trackRefs.first()
                val (trackPub) = remoteParticipant.videoTrackPublications.first()
                Assert.assertFalse(trackRef.isPlaceholder())
                Assert.assertEquals(Track.Source.CAMERA, trackRef.source)
                Assert.assertEquals(remoteParticipant, trackRef.participant)
                Assert.assertEquals(trackPub, trackRef.publication)
            }
        }

        remoteParticipant.addSubscribedMediaTrack(
            mediaTrack = MockVideoStreamTrack(),
            sid = TestData.REMOTE_VIDEO_TRACK.sid,
            statsGetter = {},
            receiver = MockRtpReceiver.create(),
            autoManageVideo = false,
            triesLeft = 1
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
