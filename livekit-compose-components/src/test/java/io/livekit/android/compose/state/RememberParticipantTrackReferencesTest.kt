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

package io.livekit.android.compose.state

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import io.livekit.android.compose.test.util.createFakeRemoteParticipant
import io.livekit.android.compose.test.util.composeTest
import io.livekit.android.room.participant.VideoTrackPublishOptions
import io.livekit.android.room.track.LocalTrackPublication
import io.livekit.android.room.track.LocalVideoTrack
import io.livekit.android.room.track.Track
import io.livekit.android.room.track.TrackPublication
import io.livekit.android.test.MockE2ETest
import io.livekit.android.test.mock.MockRtpReceiver
import io.livekit.android.test.mock.MockVideoStreamTrack
import io.livekit.android.test.mock.TestData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito

@OptIn(ExperimentalCoroutinesApi::class)
class RememberParticipantTrackReferencesTest : MockE2ETest() {

    @Test
    fun getEmptyTrackReferences() = runTest {
        connect()
        moleculeFlow(RecompositionMode.Immediate) {
            rememberParticipantTrackReferences(passedParticipant = room.localParticipant).value
        }.composeTest {
            assertTrue(awaitItem().isEmpty())
        }
    }

    @Test
    fun getPlaceholderTrackReferences() = runTest {
        connect()
        moleculeFlow(RecompositionMode.Immediate) {
            rememberParticipantTrackReferences(
                usePlaceholders = setOf(Track.Source.CAMERA),
                passedParticipant = room.localParticipant
            ).value
        }.composeTest {
            val trackRefs = awaitItem()
            assertEquals(1, trackRefs.size)
            val trackRef = trackRefs.first()
            assertTrue(trackRef.isPlaceholder())
            assertEquals(Track.Source.CAMERA, trackRef.source)
            assertEquals(room.localParticipant, trackRef.participant)
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
                ).value
            }.composeTest {
                // discard initial state.
                assertTrue(awaitItem().isEmpty())

                val trackRefs = awaitItem()
                assertEquals(1, trackRefs.size)

                val trackRef = trackRefs.first()
                val (trackPub) = room.localParticipant.videoTrackPublications.first()
                assertFalse(trackRef.isPlaceholder())
                assertEquals(Track.Source.CAMERA, trackRef.source)
                assertEquals(room.localParticipant, trackRef.participant)
                assertEquals(trackPub, trackRef.publication)
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
                rememberParticipantTrackReferences(passedParticipant = room.localParticipant).value
            }.composeTest {
                assertTrue(awaitItem().isEmpty()) // initial
                assertTrue(awaitItem().isNotEmpty()) // add
                assertTrue(awaitItem().isEmpty()) // disconnect
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
        val remoteParticipant = createFakeRemoteParticipant(coroutineRule.dispatcher)
        val job = coroutineRule.scope.launch {
            moleculeFlow(RecompositionMode.Immediate) {
                rememberParticipantTrackReferences(
                    passedParticipant = remoteParticipant,
                    onlySubscribed = true
                ).value
            }.composeTest {
                // discard initial state.
                assertTrue(awaitItem().isEmpty())

                val trackRefs = awaitItem()
                assertEquals(1, trackRefs.size)
                val trackRef = trackRefs.first()
                val (trackPub) = remoteParticipant.videoTrackPublications.first()
                assertFalse(trackRef.isPlaceholder())
                assertEquals(Track.Source.CAMERA, trackRef.source)
                assertEquals(remoteParticipant, trackRef.participant)
                assertEquals(trackPub, trackRef.publication)
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

}
