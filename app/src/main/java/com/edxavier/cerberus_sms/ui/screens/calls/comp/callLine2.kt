package com.edxavier.cerberus_sms.ui.screens.calls.comp

import android.provider.CallLog
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.edxavier.cerberus_sms.R
import com.edxavier.cerberus_sms.data.models.CallsLog
import com.edxavier.cerberus_sms.helpers.timeFormat

@Composable
fun CallLine2(call: CallsLog) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        val callTypeLabel = when (call.type) {
            CallLog.Calls.INCOMING_TYPE -> "Entrante"
            CallLog.Calls.OUTGOING_TYPE -> "Saliente"
            CallLog.Calls.MISSED_TYPE -> "Perdida"
            CallLog.Calls.BLOCKED_TYPE -> "Bloqueada"
            CallLog.Calls.REJECTED_TYPE -> "Rechazada"
            else -> "Llamada"
        }
        val typeColor = if (call.type == CallLog.Calls.MISSED_TYPE || call.type == CallLog.Calls.BLOCKED_TYPE)
            MaterialTheme.colorScheme.error
        else
            MaterialTheme.colorScheme.onSurfaceVariant

        Text(
            text = callTypeLabel,
            style = MaterialTheme.typography.labelSmall,
            color = typeColor,
            fontWeight = FontWeight.Medium
        )
        if (call.duration > 0) {
            Text(
                text = " · ",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Text(
                text = call.duration.timeFormat(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (call.total > 1) {
            Text(
                text = " · ",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Text(
                text = "[${call.total}]",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (call.sim > 0) {
            Text(
                text = " · ",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Icon(
                painter = painterResource(id = R.drawable.sim_card),
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            Text(
                text = " ${call.sim}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
