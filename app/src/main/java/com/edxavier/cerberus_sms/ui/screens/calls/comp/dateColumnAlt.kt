package com.edxavier.cerberus_sms.ui.screens.calls

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.toLowerCase
import com.edxavier.cerberus_sms.data.models.CallsLog
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CallDateAlt(
    call:CallsLog,
) {
    Column{
        val callYear = call.callDate.get(Calendar.YEAR)
        val curYear = Calendar.getInstance().get(Calendar.YEAR)
        val dateFormat = if(curYear == callYear)
            SimpleDateFormat("dd/MMM hh:mm a", Locale.getDefault())
        else
            SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
        Text(text = dateFormat.format(call.callDate.timeInMillis).lowercase(), style = MaterialTheme.typography.labelSmall)

    }

}