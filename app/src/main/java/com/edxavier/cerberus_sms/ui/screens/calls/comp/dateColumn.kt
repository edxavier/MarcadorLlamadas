package com.edxavier.cerberus_sms.ui.screens.calls

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.toLowerCase
import com.edxavier.cerberus_sms.data.models.CallsLog
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CallDate(
    call:CallsLog,
) {
    Column{
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val callYear = call.callDate.get(Calendar.YEAR)
        val curYear = Calendar.getInstance().get(Calendar.YEAR)
        val dateFormat = if(curYear == callYear)
            SimpleDateFormat("dd/MMM", Locale.getDefault())
        else
            SimpleDateFormat("dd/MMM/yy", Locale.getDefault())
        var callTime = timeFormat.format(call.callDate.timeInMillis).lowercase()
        callTime = callTime.replace("pm", "p").replace("am", "a")

        Text(text = dateFormat.format(call.callDate.timeInMillis).lowercase(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Light)

    }

}