package io.livekit.android.compose.flow

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
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

data class DataSendOptions(val reliability: DataPublishReliability, val destination: List<String>? = null)

class DataHandler(
    val messageFlow: Flow<DataMessage>,
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

data class DataMessage(val topic: String?, val payload: ByteArray, val participant: Participant?) {
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


@Composable
fun rememberDataMessageHandler(room: Room, topic: DataTopic): State<DataHandler> {
    return rememberDataMessageHandler(room, topic.value)
}

@Composable
fun rememberDataMessageHandler(room: Room, topic: String? = null): State<DataHandler> {

    val eventFlow by rememberEventSelector<RoomEvent.DataReceived>(room = room)
    val coroutineScope = rememberCoroutineScope()
    val dataHandler = remember(room, coroutineScope) {
        mutableStateOf(DataHandler(
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
                destination = options.destination,
                topic = topic
            )
        })
    }
    return dataHandler
}