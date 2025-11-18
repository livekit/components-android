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

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.InternalComposeApi
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.currentComposer
import androidx.compose.ui.platform.LocalContext

/**
 * Helper function to get the value of [block] while
 */
@Composable
fun <R> withLocalContext(context: Context, block: @Composable () -> R): R {
    return withCompositionLocal(LocalContext provides context, block)
}

// From https://android-review.googlesource.com/c/platform/frameworks/support/+/3679917
// TODO: remove when upgrading compose and this is no longer needed..
/**
 * [withCompositionLocal] binds value to [androidx.compose.runtime.ProvidableCompositionLocal] key and returns the result
 * produced by the [content] lambda. Use with non-unit returning [content] lambdas or else use
 * [CompositionLocalProvider]. Reading the [androidx.compose.runtime.CompositionLocal] using [androidx.compose.runtime.CompositionLocal.current] will
 * return the value provided in [CompositionLocalProvider]'s [value] parameter for all composable
 * functions called directly or indirectly in the [content] lambda.
 *
 * @see CompositionLocalProvider
 * @see androidx.compose.runtime.CompositionLocal
 * @see androidx.compose.runtime.compositionLocalOf
 * @see androidx.compose.runtime.staticCompositionLocalOf
 */
@Suppress("BanInlineOptIn") // b/430604046 - These APIs are stable so are ok to inline
@OptIn(InternalComposeApi::class)
@Composable
inline fun <T> withCompositionLocal(
    value: ProvidedValue<*>,
    content: @Composable () -> T,
): T {
    currentComposer.startProvider(value)
    return content().also { currentComposer.endProvider() }
}

/**
 * [withCompositionLocals] binds values to [androidx.compose.runtime.ProvidableCompositionLocal] key and returns the result
 * produced by the [content] lambda. Use with non-unit returning [content] lambdas or else use
 * [CompositionLocalProvider]. Reading the [androidx.compose.runtime.CompositionLocal] using [androidx.compose.runtime.CompositionLocal.current] will
 * return the values provided in [CompositionLocalProvider]'s [values] parameter for all composable
 * functions called directly or indirectly in the [content] lambda.
 *
 * @see CompositionLocalProvider
 * @see androidx.compose.runtime.CompositionLocal
 * @see androidx.compose.runtime.compositionLocalOf
 * @see androidx.compose.runtime.staticCompositionLocalOf
 */
@Suppress("BanInlineOptIn") // b/430604046 - These APIs are stable so are ok to inline
@OptIn(InternalComposeApi::class)
@Composable
inline fun <T> withCompositionLocals(
    vararg values: ProvidedValue<*>,
    content: @Composable () -> T,
): T {
    currentComposer.startProviders(values)
    return content().also { currentComposer.endProvider() }
}
