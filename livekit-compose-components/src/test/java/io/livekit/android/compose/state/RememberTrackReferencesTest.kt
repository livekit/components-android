package io.livekit.android.compose.state

import app.cash.molecule.RecompositionClock
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import io.livekit.android.room.track.Track
import io.livekit.android.test.MockE2ETest
import io.livekit.android.test.mock.TestData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

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
}