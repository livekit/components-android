package io.livekit.android.compose.test.util

import com.google.protobuf.ByteString
import livekit.LivekitModels.DataPacket
import livekit.LivekitModels.DataStream
import livekit.LivekitModels.DataStream.OperationType
import livekit.LivekitModels.DataStream.TextHeader
import livekit.org.webrtc.DataChannel
import java.nio.ByteBuffer


fun DataPacket.wrap() = DataChannel.Buffer(
    ByteBuffer.wrap(this.toByteArray()),
    true,
)

fun DataChannel.Observer.receiveTextStream(streamId: String = "streamId", chunk: String, topic: String = "topic") {
    receiveTextStream(streamId, listOf(chunk), topic)
}

fun DataChannel.Observer.receiveTextStream(streamId: String = "streamId", chunks: List<String>, topic: String = "topic") {
    onMessage(createStreamHeader(streamId, topic).wrap())

    for (chunk in chunks) {
        onMessage(
            createStreamChunk(
                index = 0,
                bytes = chunk.toByteArray(),
                id = streamId,
            ).wrap(),
        )
    }
    onMessage(createStreamTrailer(streamId).wrap())
}

fun createStreamHeader(id: String = "streamId", headerTopic: String = "topic") = with(DataPacket.newBuilder()) {
    streamHeader = with(DataStream.Header.newBuilder()) {
        streamId = id
        topic = headerTopic
        timestamp = 0L
        clearTotalLength()
        mimeType = "mime"

        textHeader = with(TextHeader.newBuilder()) {
            operationType = OperationType.CREATE
            generated = false
            build()
        }
        build()
    }
    build()
}

fun createStreamChunk(index: Int, bytes: ByteArray, id: String = "streamId") = with(DataPacket.newBuilder()) {
    streamChunk = with(DataStream.Chunk.newBuilder()) {
        streamId = id
        chunkIndex = index.toLong()
        content = ByteString.copyFrom(bytes)
        build()
    }
    build()
}

fun createStreamTrailer(id: String = "streamId") = with(DataPacket.newBuilder()) {
    streamTrailer = with(DataStream.Trailer.newBuilder()) {
        streamId = id
        build()
    }
    build()
}