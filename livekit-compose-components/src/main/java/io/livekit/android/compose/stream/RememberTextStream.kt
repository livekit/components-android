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

@Composable
fun rememberTextStream(topic: String, room: Room?): State<List<TextStreamData>> {
    val room = requireRoom(room)

    val coroutineScope = rememberCoroutineScope()
    val textStreamDatas = remember {
        setupTextStream(
            room, topic,
            coroutineScope = coroutineScope
        )
    }

    return textStreamDatas.collectAsState(emptyList())
}