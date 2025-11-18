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

package io.livekit.android.compose.util

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import io.livekit.android.compose.test.util.composeTest
import io.livekit.android.test.BaseTest
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RememberStateOrDefaultTest : BaseTest() {

    @Test
    fun emitsTheSameState() = runTest {
        val emitNull = mutableStateOf(false)
        val state = mutableStateOf(1)
        moleculeFlow(RecompositionMode.Immediate) {
            rememberStateOrDefault(-1) {
                if (emitNull.value) {
                    null
                } else {
                    state
                }
            }
        }.composeTest(distinctUntilChanged = false) {
            val first = awaitItem()
            emitNull.value = true
            val second = awaitItem()

            assertTrue(first === second)
        }
    }

    @Test
    fun emitsDefaultWhenNull() = runTest {
        val emitNull = mutableStateOf(false)
        val state = mutableStateOf(1)
        moleculeFlow(RecompositionMode.Immediate) {
            rememberStateOrDefault(-1) {
                if (emitNull.value) {
                    null
                } else {
                    state
                }
            }.value
        }.composeTest(distinctUntilChanged = false) {
            val first = awaitItem()
            assertEquals(1, first)

            emitNull.value = true
            val second = awaitItem()
            assertEquals(-1, second)
        }
    }

    @Test
    fun emitsStateWhenGoingToNotNull() = runTest {
        val emitNull = mutableStateOf(true)
        val state = mutableStateOf(1)
        moleculeFlow(RecompositionMode.Immediate) {
            rememberStateOrDefault(-1) {
                if (emitNull.value) {
                    null
                } else {
                    state
                }
            }.value
        }.composeTest(distinctUntilChanged = false) {
            val first = awaitItem()
            assertEquals(-1, first)

            emitNull.value = false
            val second = awaitItem()
            assertEquals(1, second)
        }
    }

    @Test
    fun emitsStateWhenReadIsSeparated() = runTest {
        val emitNull = mutableStateOf(true)
        val state = mutableStateOf(1)
        moleculeFlow(RecompositionMode.Immediate) {
            val currentEmitNull = emitNull.value
            rememberStateOrDefault(-1) {
                if (currentEmitNull) {
                    null
                } else {
                    state
                }
            }.value
        }.composeTest(distinctUntilChanged = false) {
            val first = awaitItem()
            assertEquals(-1, first)

            emitNull.value = false
            val second = awaitItem()
            assertEquals(1, second)
        }
    }

    @Test
    fun handlesCollectAsState() = runTest {
        val emitNull = mutableStateOf(true)
        val flow = MutableStateFlow(1)
        moleculeFlow(RecompositionMode.Immediate) {
            val currentEmitNull = emitNull.value
            rememberStateOrDefault(-1) {
                if (currentEmitNull) {
                    null
                } else {
                    flow.collectAsState()
                }
            }.value
        }.composeTest(distinctUntilChanged = false) {
            val first = awaitItem()
            assertEquals(-1, first)

            emitNull.value = false
            val second = awaitItem()
            assertEquals(1, second)
        }
    }

    // Verification on derivedStateOf behavior
    @Test
    fun derivedStateTest() = runTest {
        val state = mutableStateOf(1)
        moleculeFlow(RecompositionMode.Immediate) {
            remember {
                derivedStateOf {
                    state.value + 1
                }
            }.value
        }.composeTest(distinctUntilChanged = false) {
            val first = awaitItem()
            assertEquals(2, first)

            state.value = 2
            val second = awaitItem()
            assertEquals(3, second)
        }
    }

    @Test
    fun derivedStateDelegatedTest() = runTest {
        val state = mutableStateOf(1)
        moleculeFlow(RecompositionMode.Immediate) {
            val currentState by state
            remember {
                derivedStateOf {
                    currentState + 1
                }
            }.value
        }.composeTest(distinctUntilChanged = false) {
            val first = awaitItem()
            assertEquals(2, first)

            state.value = 2
            val second = awaitItem()
            assertEquals(3, second)
        }
    }
}
