package com.edxavier.cerberus_sms.ui.screens.calls

import android.provider.CallLog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.edxavier.cerberus_sms.data.models.CallsLog
import com.edxavier.cerberus_sms.helpers.getCallDirectionIcon
import com.edxavier.cerberus_sms.ui.screens.calls.comp.CallLine1
import com.edxavier.cerberus_sms.ui.screens.calls.comp.CallLine2
import com.edxavier.cerberus_sms.ui.screens.calls.comp.CallLine3

@Composable
fun HistoryLogEntry(
    call: CallsLog,
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val filter = if (call.type == CallLog.Calls.MISSED_TYPE || call.type == CallLog.Calls.BLOCKED_TYPE)
                MaterialTheme.colorScheme.error
            else
                MaterialTheme.colorScheme.secondary

            Surface(
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                color = filter.copy(alpha = 0.12f),
                tonalElevation = 2.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(id = call.type.getCallDirectionIcon()),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = filter
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                CallLine1(call = call, showNumber = true, timeOnly = true)
                Spacer(modifier = Modifier.height(4.dp))
                CallLine2(call = call)
                Spacer(modifier = Modifier.height(2.dp))
                CallLine3(call = call)
            }
        }
    }
}
