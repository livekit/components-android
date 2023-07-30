package io.livekit.android.sample.livestream.room.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.livekit.android.compose.local.RoomLocal
import io.livekit.android.compose.ui.ScaleType
import io.livekit.android.compose.ui.VideoRenderer
import io.livekit.android.room.track.VideoTrack
import io.livekit.android.sample.livestream.ui.control.Spacer

/**
 * A video grid that adapts to layouts required for 1-N participants.
 */
@Composable
fun ParticipantGrid(videoTracks: List<VideoTrack?>, isHost: Boolean, modifier: Modifier = Modifier) {
    when (videoTracks.size) {
        0 -> Box(modifier = modifier)
        1 -> SingleArrangement(videoTrack = videoTracks[0], isHost = isHost, modifier = modifier)
        2 -> TwoArrangement(videoTracks = videoTracks, isHost = isHost, modifier = modifier)
        3, 4 -> ThreeOrFourArrangement(videoTracks = videoTracks, isHost = isHost, modifier = modifier)
        else -> ManyArrangement(videoTracks = videoTracks, isHost = isHost, modifier = modifier)
    }
}

@Composable
private fun SingleArrangement(videoTrack: VideoTrack?, isHost: Boolean, modifier: Modifier) {
    // Full screen
    VideoRenderer(
        room = RoomLocal.current,
        videoTrack = videoTrack,
        scaleType = ScaleType.Fill,
        mirror = isHost,
        modifier = modifier
    )
}

@Composable
private fun TwoArrangement(videoTracks: List<VideoTrack?>, isHost: Boolean, modifier: Modifier) {
    // Vertically two stacked.
    Column(modifier = modifier) {
        VideoRenderer(
            room = RoomLocal.current,
            videoTrack = videoTracks[0],
            scaleType = ScaleType.Fill,
            mirror = isHost,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )

        Spacer(8.dp)

        VideoRenderer(
            room = RoomLocal.current,
            videoTrack = videoTracks[1],
            scaleType = ScaleType.Fill,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
    }
}

@Composable
private fun ThreeOrFourArrangement(videoTracks: List<VideoTrack?>, isHost: Boolean, modifier: Modifier) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            VideoRenderer(
                room = RoomLocal.current,
                videoTrack = videoTracks[0],
                scaleType = ScaleType.Fill,
                mirror = isHost,
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
            )

            Spacer(8.dp)

            VideoRenderer(
                room = RoomLocal.current,
                videoTrack = videoTracks[1],
                scaleType = ScaleType.Fill,
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
            )
        }

        Spacer(8.dp)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            VideoRenderer(
                room = RoomLocal.current,
                videoTrack = videoTracks[2],
                scaleType = ScaleType.Fill,
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
            )

            Spacer(8.dp)

            if (videoTracks.size > 3) {
                VideoRenderer(
                    room = RoomLocal.current,
                    videoTrack = videoTracks[3],
                    scaleType = ScaleType.Fill,
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ManyArrangement(videoTracks: List<VideoTrack?>, isHost: Boolean, modifier: Modifier) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        items(videoTracks.size) {
            VideoRenderer(
                room = RoomLocal.current,
                videoTrack = videoTracks[it],
                scaleType = ScaleType.Fill,
                mirror = isHost,
                modifier = Modifier.height(240.dp)
            )
        }
    }
}
