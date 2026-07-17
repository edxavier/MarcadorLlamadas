package com.edxavier.cerberus_sms.ui.screens.calls

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.edxavier.cerberus_sms.R
import com.edxavier.cerberus_sms.data.models.CallsLog
import com.edxavier.cerberus_sms.helpers.timeFormat

@Composable
fun CallLine2Alt(call:CallsLog) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        if( call.sim > 0){
            Icon(
                painter = painterResource(id = R.drawable.sim_card),
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(text = "${call.sim}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.width(4.dp))
            VerticalDivider(modifier = Modifier.height(12.dp))
            Spacer(modifier = Modifier.width(4.dp))
        }

        Icon(
            painter = painterResource(id = R.drawable.baseline_access_time_24),
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = call.duration.timeFormat(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(4.dp))
        VerticalDivider(modifier = Modifier.height(12.dp))
        Spacer(modifier = Modifier.width(4.dp))
        CallDateAlt(call = call)
    }
}