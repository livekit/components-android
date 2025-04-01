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
import io.livekit.android.room.participant.Participant
import io.livekit.android.test.BaseTest
import io.livekit.android.test.mock.TestData
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class RememberParticipantInfoTest : BaseTest() {

    lateinit var participant: Participant

    @Before
    fun setup() {
        participant = Participant(Participant.Sid(""), null, coroutineRule.dispatcher)
    }

    @Test
    fun getsParticipantInfo() = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
            rememberParticipantInfo(participant)
        }.test {
            val info = awaitItem()
            assertEquals(participant.identity, info.identity)
            assertEquals(participant.name, info.name)
            assertEquals(participant.metadata, info.metadata)
        }
    }

    @Test
    fun getsParticipantInfoChanges() = runTest {
        val job = coroutineRule.scope.launch {
            moleculeFlow(RecompositionMode.Immediate) {
                rememberParticipantInfo(participant)
            }.test {
                delay(10)

                val info = expectMostRecentItem()
                assertEquals(Participant.Identity(TestData.LOCAL_PARTICIPANT.identity), info.identity)
                assertEquals(TestData.LOCAL_PARTICIPANT.name, info.name)
                assertEquals(TestData.LOCAL_PARTICIPANT.metadata, info.metadata)
            }
        }
        participant.updateFromInfo(TestData.LOCAL_PARTICIPANT)
        job.join()
    }
}
