package io.livekit.android.sample.livestream.ui.control

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

@Composable
fun Spacer(size: Dp) {
    androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(size))
}