package io.livekit.android.compose.util

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import io.livekit.android.test.BaseTest
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RememberStateOrDefaultTest : BaseTest() {

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
            }
        }.test {
            val first = awaitItem()
            assertEquals(1, first.value)

            emitNull.value = true
            val second = awaitItem()
            assertTrue(first === second)
            assertEquals(-1, second.value)

            expectNoEvents()
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
            }
        }.test {
            val first = awaitItem()
            assertEquals(-1, first.value)

            emitNull.value = false
            val second = awaitItem()
            assertTrue(first === second)
            assertEquals(1, second.value)

            expectNoEvents()
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
        }.test {

            val first = awaitItem()
            assertEquals(-1, first)

            emitNull.value = false
            val second = awaitItem()
            assertEquals(1, second)

            expectNoEvents()
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
            }
        }.test {
            val first = awaitItem()
            assertEquals(-1, first.value)

            emitNull.value = false
            val second = awaitItem()
            assertTrue(first === second)
            assertEquals(1, second.value)

            expectNoEvents()
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
        }.test {
            val first = awaitItem()
            assertEquals(2, first)

            state.value = 2
            val second = awaitItem()
            assertEquals(3, second)

            expectNoEvents()
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
        }.test {
            val first = awaitItem()
            assertEquals(2, first)

            state.value = 2
            val second = awaitItem()
            assertEquals(3, second)

            expectNoEvents()
        }
    }

}