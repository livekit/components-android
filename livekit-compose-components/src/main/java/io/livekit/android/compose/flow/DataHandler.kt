/*
 * Copyright 2023-2024 LiveKit, Inc.
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

package io.livekit.android.compose.flow

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import io.livekit.android.events.RoomEvent
import io.livekit.android.room.Room
import io.livekit.android.room.participant.Participant
import io.livekit.android.room.track.DataPublishReliability
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex

data class DataSendOptions(val reliability: DataPublishReliability, val identities: List<Participant.Identity>? = null)

/**
 * A state holder for handling data messages.
 *
 * @see rememberDataMessageHandler
 */
class DataHandler(
    /** A flow for all the [DataMessage] received. */
    val messageFlow: Flow<DataMessage>,
    /** The function to call when sending a payload */
    private val send: suspend (payload: ByteArray, options: DataSendOptions) -> Unit
) {
    val isSending = mutableStateOf(false)

    private val mutex = Mutex()

    suspend fun sendMessage(payload: ByteArray, options: DataSendOptions) {
        mutex.lock()
        isSending.value = true
        send(payload, options)
        isSending.value = false
        mutex.unlock()
    }
}

/**
 * A representation of a message sent/received through LiveKit.
 */
data class DataMessage(
    /**
     * The topic channel this message is sent through.
     */
    val topic: String?,
    /**
     * The payload of the data message.
     */
    val payload: ByteArray,
    /**
     * The participant associated with this message.
     */
    val participant: Participant?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DataMessage

        if (topic != other.topic) return false
        if (!payload.contentEquals(other.payload)) return false
        if (participant != other.participant) return false

        return true
    }

    override fun hashCode(): Int {
        var result = topic?.hashCode() ?: 0
        result = 31 * result + payload.contentHashCode()
        result = 31 * result + (participant?.hashCode() ?: 0)
        return result
    }
}

/**
 * Creates a [DataHandler] that is remembered across recompositions.
 * It listens to the specified topic and emits all the messages through
 * [DataHandler.messageFlow]. Any messages sent through [DataHandler.sendMessage]
 * will be sent on the specified topic.
 */
@Composable
fun rememberDataMessageHandler(room: Room, topic: DataTopic): DataHandler {
    return rememberDataMessageHandler(room, topic.value)
}

/**
 * Creates a [DataHandler] that is remembered across recompositions.
 * It listens to the specified topic and emits all the messages through
 * [DataHandler.messageFlow]. Any messages sent through [DataHandler.sendMessage]
 * will be sent on the specified topic.
 */
@Composable
fun rememberDataMessageHandler(room: Room, topic: String? = null): DataHandler {
    val eventFlow = rememberEventSelector<RoomEvent.DataReceived>(room = room)
    val coroutineScope = rememberCoroutineScope()
    val dataHandler = remember(room, coroutineScope) {
        DataHandler(
            messageFlow = eventFlow
                .filter { event -> topic == null || event.topic == topic }
                .map { event ->
                    DataMessage(
                        topic = event.topic,
                        payload = event.data,
                        participant = event.participant
                    )
                }
        ) { payload, options ->
            room.localParticipant.publishData(
                data = payload,
                reliability = options.reliability,
                topic = topic,
                identities = options.identities,
            )
        }
    }
    return dataHandler
}
