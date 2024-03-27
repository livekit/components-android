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
import io.livekit.android.test.MockE2ETest
import io.livekit.android.test.mock.TestData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RememberRoomInfoTest : MockE2ETest() {

    @Test
    fun initialState() = runTest {
        moleculeFlow(RecompositionClock.Immediate) {
            rememberRoomInfo(room)
        }.test {
            val info = awaitItem()
            assertEquals(room.name, info.name)
            assertEquals(room.metadata, info.metadata)
        }
    }

    @Test
    fun getRoomInfoChanges() = runTest {
        val job = coroutineRule.scope.launch {
            moleculeFlow(RecompositionClock.Immediate) {
                rememberRoomInfo(room)
            }.test {
                delay(100)

                val info = expectMostRecentItem()
                val expectedRoom = TestData.JOIN.join.room
                assertEquals(expectedRoom.name, info.name)
                assertEquals(expectedRoom.metadata, info.metadata)
            }
        }
        connect()
        job.join()
    }
}
