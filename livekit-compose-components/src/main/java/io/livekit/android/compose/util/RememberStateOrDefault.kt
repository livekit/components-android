package io.livekit.android.compose.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/**
 * A utility state that either collects from the state provided from [block],
 * or emits the [default] value if null.
 *
 * The returned state will always refer to the same [State] object.
 */
@Composable
internal inline fun <T> oldrememberStateOrDefault(default: T, block: @Composable () -> State<T>?): State<T> {
    val state = block()

    val flow = snapshotFlow { state?.value }
    println("hashcode = " + flow.hashCode())
    return snapshotFlow { state?.value }
        .map { value -> value ?: default }
        .collectAsState(state?.value ?: default)
}

/**
 * A utility state that either collects from the state provided from [block],
 * or emits the [default] value if the state is null.
 *
 * The returned state will always refer to the same [State] object.
 */
@Composable
internal inline fun <T> rememberStateOrDefault(default: T, block: @Composable () -> State<T>?): State<T> {
    val blockState = rememberUpdatedState(block())

    return remember {
        derivedStateOf {
            blockState.value?.value
                ?: default
        }
    }
}