package com.edxavier.cerberus_sms.ui.screens.calls

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.edxavier.cerberus_sms.data.models.CallsLog
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CallDate(
    call:CallsLog,
) {
    Column{
        val callYear = call.callDate.get(Calendar.YEAR)
        val curYear = Calendar.getInstance().get(Calendar.YEAR)
        val dateFormat = if(curYear == callYear)
            SimpleDateFormat("dd MMM", Locale.getDefault())
        else
            SimpleDateFormat("dd MMM yy", Locale.getDefault())

        Text(
            text = dateFormat.format(call.callDate.timeInMillis),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}