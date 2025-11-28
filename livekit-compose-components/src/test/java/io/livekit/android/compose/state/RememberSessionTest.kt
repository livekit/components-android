/*
 * Copyright 2025 LiveKit, Inc.
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

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import io.livekit.android.annotations.Beta
import io.livekit.android.compose.test.util.composeTest
import io.livekit.android.test.MockE2ETest
import io.livekit.android.test.mock.TestData
import io.livekit.android.token.TokenSource
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(Beta::class)
class RememberSessionTest : MockE2ETest() {

    @Test
    fun basicSession() = runTest {
        val start by mutableStateOf(true)
        var end by mutableStateOf(false)
        val job = coroutineRule.scope.launch {
            moleculeFlow(RecompositionMode.Immediate) {
                val session = rememberSession(
                    tokenSource = TokenSource.fromLiteral(TestData.EXAMPLE_URL, "token"),
                    options = SessionOptions(
                        room = room,
                    )
                )

                LaunchedEffect(start) {
                    if (start) {
                        assertTrue(session.start().isSuccess)
                    }
                }

                LaunchedEffect(end) {
                    if (end) {
                        session.end()
                    }
                }
                session.isConnected
            }.composeTest {
                assertFalse(awaitItem())
                assertTrue(awaitItem())
                assertFalse(awaitItem())
            }
        }

        sessionConnect()

        @Suppress("AssignedValueIsNeverRead")
        end = true

        job.join()
    }

    @Test
    fun waitFunctions() = runTest {
        var end by mutableStateOf(false)
        val job = launch {
            moleculeFlow(RecompositionMode.Immediate) {
                val session = rememberSession(
                    tokenSource = TokenSource.fromLiteral(TestData.EXAMPLE_URL, "token"),
                    options = SessionOptions(
                        room = room,
                    )
                )

                var state by remember {
                    mutableStateOf(0)
                }

                LaunchedEffect(Unit) {
                    launch {
                        session.waitUntilConnected()
                        state = 1
                    }
                }
                LaunchedEffect(Unit) {
                    launch {
                        session.waitUntilDisconnected()
                        state = 2
                    }
                }
                LaunchedEffect(Unit) {
                    assertTrue(session.start().isSuccess)
                }
                LaunchedEffect(end) {
                    if (end) {
                        session.end()
                    }
                }

                state
            }.composeTest {
                assertEquals(0, awaitItem())
                assertEquals(1, awaitItem())
                assertEquals(2, awaitItem())
            }
        }

        sessionConnect()

        @Suppress("AssignedValueIsNeverRead")
        end = true

        job.join()
    }
}

suspend fun MockE2ETest.sessionConnect() {
    prepareSignal()
    connectPeerConnection()
}
