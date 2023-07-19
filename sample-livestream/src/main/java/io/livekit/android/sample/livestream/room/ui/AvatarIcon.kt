package io.livekit.android.sample.livestream.room.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import coil.compose.AsyncImage
import io.livekit.android.sample.livestream.room.screen.nameToColor

@Composable
fun AvatarIcon(imageUrl: String, name: String?, modifier: Modifier = Modifier) {
    if (imageUrl.isNotEmpty()) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            modifier = modifier
        )
    } else {
        Canvas(modifier = modifier, onDraw = {
            drawCircle(color = nameToColor(name))
        })
    }
}