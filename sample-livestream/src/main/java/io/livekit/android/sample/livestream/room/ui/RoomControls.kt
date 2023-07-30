@file:OptIn(ExperimentalMaterial3Api::class)

package io.livekit.android.sample.livestream.room.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import io.livekit.android.sample.livestream.R
import io.livekit.android.sample.livestream.room.screen.RoomScreen
import io.livekit.android.sample.livestream.ui.control.Spacer
import io.livekit.android.sample.livestream.ui.theme.Dimens
import io.livekit.android.sample.livestream.ui.theme.Indicator
import io.livekit.android.sample.livestream.ui.theme.LKTextStyle

/**
 * A composable for displaying all the controls shown at the top of [RoomScreen]
 */
@Composable
fun RoomControls(
    showFlipButton: Boolean,
    participantCount: Int,
    showParticipantIndicator: Boolean,
    onFlipButtonClick: () -> Unit,
    onParticipantButtonClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ConstraintLayout(modifier = modifier) {
        val (flipButton, liveButton, participantCountButton) = createRefs()

        if (showFlipButton) {
            ControlButton(
                onClick = onFlipButtonClick,
                modifier = Modifier.constrainAs(flipButton) {
                    width = Dimension.value(43.dp)
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        ParticipantCountButton(
            participantCount = participantCount,
            showIndicator = showParticipantIndicator,
            onClick = onParticipantButtonClick,
            modifier = Modifier.constrainAs(participantCountButton) {
                end.linkTo(parent.end)
                top.linkTo(parent.top)
            }
        )

        ControlButton(
            onClick = {},
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.constrainAs(liveButton) {
                end.linkTo(participantCountButton.start, 8.dp)
                top.linkTo(parent.top)
            }
        ) {
            Text(
                text = "LIVE",
                style = LKTextStyle.roomButton.copy(letterSpacing = 0.6.sp),
            )
        }
    }
}

@Composable
private fun ParticipantCountButton(
    participantCount: Int,
    onClick: () -> Unit,
    showIndicator: Boolean,
    modifier: Modifier = Modifier,
) {
    ControlButton(onClick = onClick, modifier = modifier
        .drawWithContent {
            drawContent()

            if (showIndicator) {
                drawCircle(
                    color = Indicator,
                    radius = 16.dp.value,
                    center = Offset(size.width - 6.dp.value, 30.dp.value)
                )
            }
        }) {
        Image(
            painter = painterResource(id = R.drawable.eye),
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )
        Spacer(size = 6.dp)
        Text(
            text = participantCount.toString(),
            style = LKTextStyle.roomButton,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ControlButton(
    modifier: Modifier = Modifier,
    color: Color = Color(0x80808080),
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit
) {

    Surface(
        shape = RoundedCornerShape(Dimens.smallButtonCornerRadius),
        onClick = onClick,
        modifier = modifier
            .defaultMinSize(1.dp, 1.dp)
            .padding(0.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .background(color)
                .padding(horizontal = 8.dp, vertical = 0.dp)
                .height(28.dp)
        ) {
            content()
        }
    }
}