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

package io.livekit.android.compose.stream

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import io.livekit.android.compose.flow.TextStreamData
import io.livekit.android.compose.flow.setupTextStream
import io.livekit.android.compose.local.requireRoom
import io.livekit.android.room.Room

/**
 * Registers a text stream handler for [topic], and returns a state containing a list
 * with a [TextStreamData] for each received stream.
 *
 * @param topic The topic to listen for text streams.
 * @param room The room to register on, or [io.livekit.android.compose.local.RoomLocal] if none is passed.
 */
@Composable
fun rememberTextStream(topic: String, room: Room? = null): State<List<TextStreamData>> {
    val room = requireRoom(room)

    val coroutineScope = rememberCoroutineScope()
    val textStreamDatas = remember {
        setupTextStream(
            room,
            topic,
            coroutineScope = coroutineScope
        )
    }

    return textStreamDatas.collectAsState(emptyList())
}
