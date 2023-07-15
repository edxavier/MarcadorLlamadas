package com.edxavier.cerberus_sms.ui.screens.calls.comp

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edxavier.cerberus_sms.data.models.CallsLog
import com.edxavier.cerberus_sms.helpers.getOperatorColor
import com.edxavier.cerberus_sms.helpers.toPhoneFormat

@Composable
fun CallLine1(call:CallsLog, showNumber: Boolean = false) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        call.operator?.let {
            Card(
                colors = CardDefaults.cardColors(
                    Color(it.operator.getOperatorColor(LocalContext.current))
                ),
                modifier = Modifier.size(8.dp)
            ) {}
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = if(showNumber) call.number.toPhoneFormat() else call.name,
            overflow = TextOverflow.Ellipsis,
            softWrap = false,
            color = if(call.isBlocked) Color.Red else MaterialTheme.colorScheme.onSurface,
            fontSize = 16.sp
        )
    }
}