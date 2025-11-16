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
import io.livekit.android.room.Room
import io.livekit.android.test.MockE2ETest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RememberConnectionStateTest : MockE2ETest() {

    @Test
    fun initialState() = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
            rememberConnectionState(room).value
        }.test {
            assertEquals(awaitItem(), Room.State.DISCONNECTED)
        }
    }

    @Test
    fun connectAndDisconnectState() = runTest {
        val job = coroutineRule.scope.launch {
            moleculeFlow(RecompositionMode.Immediate) {
                rememberConnectionState(room).value
            }.test {
                assertEquals(awaitItem(), Room.State.DISCONNECTED)
                assertEquals(awaitItem(), Room.State.CONNECTING)
                assertEquals(awaitItem(), Room.State.CONNECTED)
                assertEquals(awaitItem(), Room.State.DISCONNECTED)
            }
        }

        connect()
        room.disconnect()

        job.join()
    }
}
