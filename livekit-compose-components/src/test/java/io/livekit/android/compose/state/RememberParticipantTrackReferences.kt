package io.livekit.android.compose.state

import app.cash.molecule.RecompositionClock
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import io.livekit.android.room.participant.VideoTrackPublishOptions
import io.livekit.android.room.track.LocalTrackPublication
import io.livekit.android.room.track.LocalVideoTrack
import io.livekit.android.room.track.Track
import io.livekit.android.room.track.TrackPublication
import io.livekit.android.test.MockE2ETest
import io.livekit.android.test.mock.TestData
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
        moleculeFlow(RecompositionClock.Immediate) {
            rememberParticipantTrackReferences(passedParticipant = room.localParticipant)
        }.test {
            Assert.assertTrue(awaitItem().isEmpty())
        }
    }

    @Test
    fun getPlaceholderTrackReferences() = runTest {
        connect()
        moleculeFlow(RecompositionClock.Immediate) {
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
            moleculeFlow(RecompositionClock.Immediate) {
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
            moleculeFlow(RecompositionClock.Immediate) {
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
}