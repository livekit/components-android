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
import io.livekit.android.compose.test.util.composeTest
import io.livekit.android.test.MockE2ETest
import io.livekit.android.test.mock.TestData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RememberRoomInfoTest : MockE2ETest() {

    @Test
    fun initialState() = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
            rememberRoomInfo(room)
        }.composeTest {
            val info = awaitItem()
            assertEquals(room.name, info.name)
            assertEquals(room.metadata, info.metadata)
        }
    }

    @Test
    fun getRoomInfoChanges() = runTest {
        val job = coroutineRule.scope.launch {
            moleculeFlow(RecompositionMode.Immediate) {
                val info = rememberRoomInfo(room)
                info.name to info.metadata
            }.composeTest {
                awaitItem()
                val (name, metadata) = awaitItem()
                val expectedRoom = TestData.JOIN.join.room
                assertEquals(expectedRoom.name, name)
                assertEquals(expectedRoom.metadata, metadata)

                expectNoEvents()
            }
        }
        connect()
        job.join()
    }
}
