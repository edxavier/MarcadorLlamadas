package com.edxavier.cerberus_sms.ui.screens.dialer

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DialKeyboard(
    mainText: String = "",
    secondaryText: String = "",
    enableLongKeyPress: Boolean = false,
    textColor: Color = MaterialTheme.colorScheme.tertiary,
    fontSize: TextUnit = 24.sp,
    fontWeight: FontWeight = FontWeight.Normal,
    onKeyPress: (key:String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(
                onClick = { onKeyPress(mainText) },
                onLongClick = {
                    if(enableLongKeyPress)
                        onKeyPress(secondaryText)
                }
            )
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ){
        Text(text = mainText, fontSize = fontSize, color = textColor, fontWeight = fontWeight)
        Text(text = secondaryText, fontSize = 8.sp, fontWeight = FontWeight.Light)
    }
}