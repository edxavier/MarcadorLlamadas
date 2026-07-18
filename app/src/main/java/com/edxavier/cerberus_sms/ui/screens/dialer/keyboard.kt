package com.edxavier.cerberus_sms.ui.screens.dialer

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DialKeyboard(
    mainText: String = "",
    secondaryText: String = "",
    enableLongKeyPress: Boolean = false,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    fontSize: TextUnit = 24.sp,
    fontWeight: FontWeight = FontWeight.Normal,
    keySize: Dp = 64.dp,
    onKeyPress: (key: String) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = tween(durationMillis = 120),
        label = "keyScale"
    )

    Box(
        modifier = Modifier
            .size(keySize)
            .scale(scale)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { onKeyPress(mainText) },
                onLongClick = {
                    if (enableLongKeyPress) onKeyPress(secondaryText)
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = mainText,
                fontSize = fontSize,
                color = textColor,
                fontWeight = fontWeight
            )
            if (secondaryText.isNotBlank()) {
                Text(
                    text = secondaryText,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
