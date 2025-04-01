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
import app.cash.turbine.test
import io.livekit.android.compose.types.TrackReference
import io.livekit.android.room.track.Track
import io.livekit.android.test.MockE2ETest
import io.livekit.android.test.mock.TestData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import livekit.LivekitRtc
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RememberTrackMutedTest : MockE2ETest() {
    @Test
    fun notMuted() = runTest {
        connect()
        simulateMessageFromServer(TestData.PARTICIPANT_JOIN)

        val remoteParticipant = room.remoteParticipants.values.first()
        val publication = remoteParticipant.trackPublications.values.first { it.kind == Track.Kind.VIDEO }
        val trackReference = TrackReference(
            participant = remoteParticipant,
            source = Track.Source.fromProto(TestData.REMOTE_VIDEO_TRACK.source),
            publication = publication,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            rememberTrackMuted(trackRef = trackReference)
        }.test {
            assertFalse(awaitItem())
        }
    }

    @Test
    fun placeHolderIsMuted() = runTest {
        connect()
        simulateMessageFromServer(TestData.PARTICIPANT_JOIN)

        val remoteParticipant = room.remoteParticipants.values.first()
        val trackReference = TrackReference(
            participant = remoteParticipant,
            source = Track.Source.SCREEN_SHARE,
            publication = null,
        )

        moleculeFlow(RecompositionMode.Immediate) {
            rememberTrackMuted(trackReference)
        }.test {
            delay(10)
            val isMuted = expectMostRecentItem()
            assertTrue(isMuted)
        }
    }

    @Test
    fun mutedParticipant() = runTest {
        connect()
        simulateMessageFromServer(TestData.PARTICIPANT_JOIN)

        val remoteParticipant = room.remoteParticipants.values.first()
        val publication = remoteParticipant.trackPublications.values.first { it.kind == Track.Kind.VIDEO }
        val trackReference = TrackReference(
            participant = remoteParticipant,
            source = Track.Source.SCREEN_SHARE,
            publication = publication,
        )

        val job = coroutineRule.scope.launch {
            moleculeFlow(RecompositionMode.Immediate) {
                rememberTrackMuted(trackReference)
            }.test {
                assertFalse(awaitItem())
                assertTrue(awaitItem())
            }
        }

        val mutedParticipant = with(TestData.REMOTE_PARTICIPANT.toBuilder()) {
            val newTracksList = tracksList.map { trackInfo ->
                with(trackInfo.toBuilder()) {
                    muted = true
                    build()
                }
            }
            clearTracks()
            addAllTracks(newTracksList)
        }
        val muteMessage = with(LivekitRtc.SignalResponse.newBuilder()) {
            update = with(LivekitRtc.ParticipantUpdate.newBuilder()) {
                addParticipants(mutedParticipant)
                build()
            }
            build()
        }

        simulateMessageFromServer(muteMessage)
        job.join()
    }
}
