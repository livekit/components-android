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
        moleculeFlow(RecompositionMode.Immediate) {
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
            moleculeFlow(RecompositionMode.Immediate) {
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
            moleculeFlow(RecompositionMode.Immediate) {
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
