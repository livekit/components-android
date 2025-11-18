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

package io.livekit.android.compose.test.util

import app.cash.turbine.TurbineTestContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.yield
import kotlin.time.Duration
import app.cash.turbine.test as turbineTest

/**
 * Due to the use of [kotlinx.coroutines.test.UnconfinedTestDispatcher],
 * Turbine fails to validate unconsumed events. This yields at the end of
 * the validation block to release the coroutine to finish any extra work.
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
            yield()
        }
    )
}
