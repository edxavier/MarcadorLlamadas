package com.edxavier.cerberus_sms.ui.screens.calls

import android.provider.CallLog
import androidx.compose.foundation.layout.*
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
import com.edxavier.cerberus_sms.ui.screens.calls.comp.CallLine3
@Composable
fun HistoryLogEntry(
    call: CallsLog,
) {

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val filter = if(call.type == CallLog.Calls.MISSED_TYPE || call.type == CallLog.Calls.BLOCKED_TYPE)
                MaterialTheme.colorScheme.error
            else
                MaterialTheme.colorScheme.secondary
            Icon(
                painter = painterResource(id = call.type.getCallDirectionIcon()),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = filter
            )
            Column(modifier = Modifier.weight(1f)){
                CallLine1(call = call, showNumber = true)
                CallLine2Alt(call = call)
                Spacer(modifier = Modifier.height(2.dp))
                CallLine3(call = call)
            }
        }
    }




}