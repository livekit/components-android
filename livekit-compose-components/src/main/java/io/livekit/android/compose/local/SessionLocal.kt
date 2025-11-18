/*
 * Copyright 2023-2025 LiveKit, Inc.
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

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import io.livekit.android.annotations.Beta
import io.livekit.android.compose.state.Session
import io.livekit.android.room.participant.LocalParticipant

/**
 * Not to be confused with [LocalParticipant].
 */
@Beta
@SuppressLint("CompositionLocalNaming")
val SessionLocal =
    compositionLocalOf<Session> { throw IllegalStateException("No Session object available. This should only be used within a SessionScope.") }

/**
 * Establishes a session scope which allows the current [Session] that can be accessed
 * through the [SessionLocal] composition local, as well as the session's room object
 * through the [RoomLocal] composition local.
 */
@Beta
@Composable
fun SessionScope(
    session: Session,
    content: @Composable (session: Session) -> Unit
) {
    CompositionLocalProvider(
        SessionLocal provides session,
        RoomLocal provides session.room,
        content = { content(session) },
    )
}

/**
 * Returns the [session], or if null/no-arg, the currently provided [SessionLocal].
 * @throws IllegalStateException if [session] is null and no [SessionLocal] is available (e.g. not inside a [SessionScope]).
 */
@Beta
@Composable
@Throws(IllegalStateException::class)
fun requireSession(session: Session? = null): Session {
    return session ?: SessionLocal.current
}
