/*
 * Copyright 2023 LiveKit, Inc.
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

package io.livekit.android.compose.chat

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import io.livekit.android.compose.flow.DataHandler
import io.livekit.android.compose.flow.DataSendOptions
import io.livekit.android.compose.flow.DataTopic
import io.livekit.android.compose.flow.rememberDataMessageHandler
import io.livekit.android.compose.local.RoomLocal
import io.livekit.android.room.Room
import io.livekit.android.room.participant.LocalParticipant
import io.livekit.android.room.participant.Participant
import io.livekit.android.room.track.DataPublishReliability
import kotlinx.coroutines.sync.Mutex
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.Date

class Chat(private val localParticipant: LocalParticipant, private val dataHandler: DataHandler) {
    private val stateLock = Mutex()

    suspend fun send(message: String) {
        val timestamp = Date().time

        // participant is filled in server side.
        val chatMessage = ChatMessage(
            timestamp = timestamp,
            message = message,
        )

        val encodedMessage = Json.encodeToString(chatMessage).toByteArray(Charsets.UTF_8)
        dataHandler.sendMessage(
            payload = encodedMessage,
            options = DataSendOptions(reliability = DataPublishReliability.RELIABLE)
        )

        // add the messages to the local log.
        addMessage(chatMessage.copy(participant = localParticipant))
    }

    internal suspend fun addMessage(chatMessage: ChatMessage) {
        stateLock.lock()
        messages.value = messages.value.plus(chatMessage)
        stateLock.unlock()
    }

    val isSending: State<Boolean>
        get() = dataHandler.isSending
    val messages = mutableStateOf(emptyList<ChatMessage>())
}

@Serializable
data class ChatMessage(
    /** millis since UNIX epoch */
    val timestamp: Long,
    val message: String,
    @Transient
    val participant: Participant? = null
)

@Composable
fun rememberChat(room: Room = RoomLocal.current): State<Chat> {
    val dataHandler by rememberDataMessageHandler(room = room, topic = DataTopic.CHAT)
    val chatState = remember(dataHandler) {
        mutableStateOf(
            Chat(
                localParticipant = room.localParticipant,
                dataHandler = dataHandler,
            )
        )
    }

    LaunchedEffect(dataHandler, chatState) {
        dataHandler.messageFlow
            .collect { dataMessage ->
                val payloadString = dataMessage.payload.decodeToString()
                try {
                    val chatMessage = Json.decodeFromString<ChatMessage>(payloadString)
                        .copy(participant = dataMessage.participant)

                    chatState.value.addMessage(chatMessage)
                } catch (e: Exception) {
                    Log.w("Chat", "malformed chat message: $payloadString")
                }
            }
    }

    return chatState
}
