package com.edxavier.cerberus_sms.ui.screens.calls.comp

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import com.edxavier.cerberus_sms.data.models.CallsLog

@Composable
fun CallLine3(call:CallsLog) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        var tmp = ""
        call.operator?.let {
            tmp = if(it.area.isNotBlank())
                "${it.area}, ${it.country}"
            else
                it.country
            Text(text = tmp, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Light)
        }
    }
}