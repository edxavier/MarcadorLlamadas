package com.edxavier.cerberus_sms.ui.screens.calls

import android.provider.CallLog
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.edxavier.cerberus_sms.data.models.CallsLog
import com.edxavier.cerberus_sms.helpers.getCallDirectionIcon
import com.edxavier.cerberus_sms.ui.screens.calls.comp.CallLine1
import com.edxavier.cerberus_sms.ui.screens.calls.comp.CallLine3

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HistoryLogEntry(
    call: CallsLog,
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {
                    // context.makeCall(call.number)
                }
            )
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ){
        val filter = if(call.type == CallLog.Calls.MISSED_TYPE || call.type == CallLog.Calls.BLOCKED_TYPE)
            Color.Red
        else
            MaterialTheme.colorScheme.secondary
        Image(
            painter = painterResource(id = call.type.getCallDirectionIcon()),
            contentDescription = null, modifier = Modifier.size(18.dp),
            colorFilter = ColorFilter.tint(filter)
        )
        Column(modifier = Modifier.weight(1f)){
            CallLine1(call = call, showNumber = true)
            CallLine2Alt(call = call)
            Spacer(modifier = Modifier.height(2.dp))
            CallLine3(call = call)
        }
    }




}