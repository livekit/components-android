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

package io.livekit.android.compose.local

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import io.livekit.android.compose.test.util.composeTest
import io.livekit.android.room.Room
import io.livekit.android.test.MockE2ETest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RoomScopeTest : MockE2ETest() {

    @Test
    fun passesBackRoom() = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
            var testRoom: Room? = null
            RoomScopeSetup {
                RoomScope(connect = false, passedRoom = room) {
                    testRoom = it
                }
            }
            testRoom
        }.composeTest {
            val retRoom = awaitItem()
            assertNotNull(retRoom)
        }
    }

    @Test
    fun roomCreateIfNotPassed() = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
            var testRoom: Room? = null
            RoomScopeSetup {
                RoomScope(connect = false) {
                    testRoom = it
                }
            }
            testRoom
        }.composeTest {
            awaitError() // real rooms can't be created in test env.
        }
    }

    @Test
    fun disconnectOnDispose() = runTest {
        // mock room needs connecting manually
        connect()
        moleculeFlow(RecompositionMode.Immediate) {
            var useScope by remember { mutableStateOf(true) }
            LaunchedEffect(Unit) {
                delay(1)
                useScope = false
            }
            if (useScope) {
                RoomScopeSetup {
                    RoomScope(
                        connect = true,
                        disconnectOnDispose = true,
                        passedRoom = room
                    ) {}
                }
            }
            useScope
        }.composeTest {
            awaitItem()
            assertEquals(Room.State.CONNECTED, room.state)
            delay(1000)
            awaitItem()
            assertEquals(Room.State.DISCONNECTED, room.state)
        }
    }

    @Test
    fun noDisconnectOnDispose() = runTest {
        // mock room needs connecting manually
        connect()
        moleculeFlow(RecompositionMode.Immediate) {
            var useScope by remember { mutableStateOf(true) }
            LaunchedEffect(Unit) {
                delay(1)
                useScope = false
            }
            if (useScope) {
                RoomScopeSetup {
                    RoomScope(
                        connect = true,
                        disconnectOnDispose = false,
                        passedRoom = room
                    ) {
                    }
                }
            }
            1
        }.composeTest {
            awaitItem()
            assertEquals(Room.State.CONNECTED, room.state)
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
