package io.livekit.android.sample.livestream.room.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import io.livekit.android.compose.local.RoomLocal
import io.livekit.android.compose.ui.ScaleType
import io.livekit.android.compose.ui.VideoRenderer
import io.livekit.android.room.track.VideoTrack
import io.livekit.android.sample.livestream.R
import io.livekit.android.sample.livestream.ui.control.Spacer
import io.livekit.android.sample.livestream.ui.theme.NoVideoBackground

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
    VideoItem(
        videoTrack = videoTrack,
        isHost = isHost,
        modifier = modifier
    )
}

@Composable
private fun TwoArrangement(videoTracks: List<VideoTrack?>, isHost: Boolean, modifier: Modifier) {
    // Vertically two stacked.
    Column(modifier = modifier) {
        VideoItem(
            videoTrack = videoTracks[0],
            isHost = isHost,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )

        Spacer(8.dp)

        VideoItem(
            videoTrack = videoTracks[1],
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
            VideoItem(
                videoTrack = videoTracks[0],
                isHost = isHost,
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
            )

            Spacer(8.dp)

            VideoItem(
                videoTrack = videoTracks[1],
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
            VideoItem(
                videoTrack = videoTracks[2],
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
            )

            Spacer(8.dp)

            if (videoTracks.size > 3) {
                VideoItem(
                    videoTrack = videoTracks[3],
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                ) {}
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
        items(videoTracks.size) { index ->
            VideoItem(
                videoTrack = videoTracks[index],
                isHost = index == 0 && isHost,
                modifier = Modifier.height(240.dp)
            )
        }
    }
}

@Composable
fun VideoItem(videoTrack: VideoTrack?, modifier: Modifier = Modifier, isHost: Boolean = false) {

    if (videoTrack == null) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .clip(RoundedCornerShape(8.dp))
        ) {
            Box(modifier = Modifier.background(NoVideoBackground).fillMaxSize())
            Image(
                painter = painterResource(id = R.drawable.outline_videocam_off_24),
                contentDescription = "",
                modifier = Modifier.fillMaxSize(0.4f)
            )
        }
    } else {
        VideoRenderer(
            room = RoomLocal.current,
            videoTrack = videoTrack,
            mirror = isHost,
            scaleType = ScaleType.Fill,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .then(modifier)
        )
    }
}