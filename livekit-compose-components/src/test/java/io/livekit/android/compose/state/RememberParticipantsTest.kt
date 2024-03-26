package io.livekit.android.compose.state

import app.cash.molecule.RecompositionClock
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import io.livekit.android.room.participant.LocalParticipant
import io.livekit.android.room.participant.Participant
import io.livekit.android.test.MockE2ETest
import io.livekit.android.test.mock.TestData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RememberParticipantsTest : MockE2ETest() {

    @Test
    fun initialState() = runTest {
        moleculeFlow(RecompositionClock.Immediate) {
            rememberParticipants(room)
        }.test {
            val participants = awaitItem()
            assertEquals(participants.size, 1)
            assertEquals(participants.first(), room.localParticipant)
        }
    }

    @Test
    fun participantJoin() = runTest {
        val job = coroutineRule.scope.launch {
            moleculeFlow(RecompositionClock.Immediate) {
                rememberParticipants(room)
            }.test {
                delay(10)
                val participants = expectMostRecentItem()
                assertEquals(participants.size, 2)
                assertTrue(participants.contains(room.localParticipant))

                val remoteParticipant = participants.first { p -> p !is LocalParticipant }
                assertEquals(Participant.Identity(TestData.REMOTE_PARTICIPANT.identity), remoteParticipant.identity)
            }
        }

        connect()
        simulateMessageFromServer(TestData.PARTICIPANT_JOIN)

        job.join()
    }

    @Test
    fun participantLeave() = runTest {
        val job = coroutineRule.scope.launch {
            moleculeFlow(RecompositionClock.Immediate) {
                rememberParticipants(room)
            }.test {
                delay(10)
                val participants = expectMostRecentItem()
                assertEquals(participants.size, 1)
                assertEquals(participants.first(), room.localParticipant)
            }
        }

        connect()
        simulateMessageFromServer(TestData.PARTICIPANT_JOIN)
        simulateMessageFromServer(TestData.PARTICIPANT_DISCONNECT)

        job.join()
    }
}