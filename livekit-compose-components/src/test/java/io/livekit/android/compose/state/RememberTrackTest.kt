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
import io.livekit.android.compose.types.TrackReference
import io.livekit.android.room.track.RemoteVideoTrack
import io.livekit.android.room.track.Track
import io.livekit.android.room.track.VideoTrack
import io.livekit.android.test.MockE2ETest
import io.livekit.android.test.assert.assertIsClass
import io.livekit.android.test.mock.MockMediaStream
import io.livekit.android.test.mock.MockRtpReceiver
import io.livekit.android.test.mock.MockVideoStreamTrack
import io.livekit.android.test.mock.TestData
import io.livekit.android.test.mock.createMediaStreamId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RememberTrackTest : MockE2ETest() {
    @Test
    fun initialState() = runTest {
        connect()
        simulateMessageFromServer(TestData.PARTICIPANT_JOIN)

        val remoteParticipant = room.remoteParticipants.values.first()
        val publication = remoteParticipant.trackPublications.values.first { it.kind == Track.Kind.VIDEO }
        val trackReference = TrackReference(
            participant = remoteParticipant,
            source = Track.Source.fromProto(TestData.REMOTE_VIDEO_TRACK.source),
            publication = publication,
        )
        moleculeFlow(RecompositionClock.Immediate) {
            rememberTrack<VideoTrack>(trackReference)
        }.test {
            val track = awaitItem()
            assertNull(track)
        }
    }

    @Test
    fun getRoomInfoChanges() = runTest {
        connect()
        simulateMessageFromServer(TestData.PARTICIPANT_JOIN)

        val remoteParticipant = room.remoteParticipants.values.first()
        val publication = remoteParticipant.trackPublications.values.first { it.kind == Track.Kind.VIDEO }
        val trackReference = TrackReference(
            participant = remoteParticipant,
            source = Track.Source.fromProto(TestData.REMOTE_VIDEO_TRACK.source),
            publication = publication,
        )

        val videoTrack = MockVideoStreamTrack()

        val job = coroutineRule.scope.launch {
            moleculeFlow(RecompositionClock.Immediate) {
                rememberTrack<VideoTrack>(trackReference)
            }.test {
                delay(10)
                val track = expectMostRecentItem()
                assertIsClass(RemoteVideoTrack::class.java, track!!)
                assertEquals(videoTrack, track.rtcTrack)
            }
        }
        room.onAddTrack(
            MockRtpReceiver.create(),
            videoTrack,
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
}
