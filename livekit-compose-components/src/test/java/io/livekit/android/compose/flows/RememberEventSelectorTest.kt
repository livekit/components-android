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

package io.livekit.android.compose.flows

import androidx.compose.runtime.collectAsState
import app.cash.molecule.RecompositionClock
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import io.livekit.android.compose.flow.rememberEventSelector
import io.livekit.android.events.RoomEvent
import io.livekit.android.test.MockE2ETest
import io.livekit.android.test.assert.assertIsClass
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RememberEventSelectorTest : MockE2ETest() {

    @Test
    fun getsEvents() = runTest {
        val job = coroutineRule.scope.launch {
            moleculeFlow(RecompositionClock.Immediate) {
                rememberEventSelector<RoomEvent>(room = room).collectAsState(initial = null).value
            }.test {
                // discard initial state.
                awaitItem()
                val event = awaitItem()
                assertIsClass(RoomEvent.Connected::class.java, event)
                ensureAllEventsConsumed()
            }
        }
        connect()
        job.join()
    }
}
