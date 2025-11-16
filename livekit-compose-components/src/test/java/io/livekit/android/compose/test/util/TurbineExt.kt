package io.livekit.android.compose.test.util

import app.cash.turbine.TurbineTestContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.time.Duration
import app.cash.turbine.test as turbineTest

/**
 * Due to the use of [kotlinx.coroutines.test.UnconfinedTestDispatcher],
 * Turbine fails to validate unconsumed events. This adds a small delay
 * at the end of the validation block to release the coroutine to finish
 * any extra work.
 */
suspend fun <T> Flow<T>.composeTest(
    distinctUntilChanged: Boolean = true,
    timeout: Duration? = null,
    name: String? = null,
    validate: suspend TurbineTestContext<T>.() -> Unit
) {
    val flow = if (distinctUntilChanged) {
        this.distinctUntilChanged()
    } else {
        this
    }
    flow.turbineTest(
        timeout,
        name,
        {
            validate()
            delay(1)
        }
    )
}