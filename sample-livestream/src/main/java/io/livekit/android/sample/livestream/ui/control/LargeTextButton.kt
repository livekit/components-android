package io.livekit.android.sample.livestream.ui.control

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.livekit.android.sample.livestream.ui.theme.Dimens
import io.livekit.android.sample.livestream.ui.theme.LKTextStyle

@Composable
fun LargeTextButton(
    modifier: Modifier = Modifier,
    text: String = "",
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    onClick: () -> Unit,
) {
    Button(
        colors = colors,
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .height(Dimens.buttonHeight)
            .then(modifier)
    ) {
        Text(
            text = text,
            style = LKTextStyle.largeButton,
        )
    }
}