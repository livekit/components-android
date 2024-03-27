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

package io.livekit.android.compose.local

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import app.cash.molecule.RecompositionClock
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import io.livekit.android.room.Room
import io.livekit.android.test.MockE2ETest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertNotNull
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RoomScopeTest : MockE2ETest() {

    @Test
    fun passesBackRoom() = runTest {
        moleculeFlow(RecompositionClock.Immediate) {
            var testRoom: Room? = null
            RoomScopeSetup {
                RoomScope(connect = false, passedRoom = room) {
                    testRoom = it
                }
            }
            testRoom
        }.test {
            val retRoom = awaitItem()
            assertNotNull(retRoom)
        }
    }

    @Test
    fun roomCreateIfNotPassed() = runTest {
        moleculeFlow(RecompositionClock.Immediate) {
            var testRoom: Room? = null
            RoomScopeSetup {
                RoomScope(connect = false) {
                    testRoom = it
                }
            }
            testRoom
        }.test {
            awaitError() // real rooms can't be created in test env.
        }
    }

    /**
     * RoomScope requires a LocalContext, which would normally be
     * provided in a ComposeView.
     */
    @Composable
    fun RoomScopeSetup(content: @Composable () -> Unit) {
        CompositionLocalProvider(
            LocalContext provides context
        ) {
            content()
        }
    }
}
