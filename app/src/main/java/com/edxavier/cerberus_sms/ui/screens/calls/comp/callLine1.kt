package com.edxavier.cerberus_sms.ui.screens.calls.comp

import android.provider.CallLog
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edxavier.cerberus_sms.data.models.CallsLog
import com.edxavier.cerberus_sms.helpers.getOperatorColor
import com.edxavier.cerberus_sms.helpers.toPhoneFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CallLine1(call: CallsLog, showNumber: Boolean = false, timeOnly: Boolean = false) {
    val context = LocalContext.current
    val now = Calendar.getInstance()
    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val yesterday = (today.clone() as Calendar).apply {
        add(Calendar.DAY_OF_YEAR, -1)
    }
    val weekAgo = (today.clone() as Calendar).apply {
        add(Calendar.DAY_OF_YEAR, -7)
    }

    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val timeText = timeFormat.format(call.callDate.timeInMillis).lowercase()
    val callYear = call.callDate.get(Calendar.YEAR)
    val curYear = now.get(Calendar.YEAR)

    val isToday = call.callDate.timeInMillis >= today.timeInMillis
    val isYesterday = call.callDate.before(today) && call.callDate.after(yesterday)
    val isThisWeek = call.callDate.before(today) && call.callDate.after(weekAgo)

    val dateText: String
    val showTime: Boolean

    when {
        isToday -> {
            dateText = "Hoy"
            showTime = true
        }
        isYesterday -> {
            dateText = "Ayer"
            showTime = true
        }
        isThisWeek -> {
            val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault())
            dateText = dayFormat.format(call.callDate.timeInMillis).replaceFirstChar { it.uppercase() }
            showTime = true
        }
        else -> {
            dateText = if (curYear == callYear) {
                SimpleDateFormat("dd MMM", Locale.getDefault()).format(call.callDate.timeInMillis)
            } else {
                SimpleDateFormat("dd MMM yy", Locale.getDefault()).format(call.callDate.timeInMillis)
            }
            showTime = false
        }
    }

    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = if (showNumber) call.number.toPhoneFormat() else call.name,
            overflow = TextOverflow.Ellipsis,
            softWrap = false,
            color = when {
                call.isBlocked -> MaterialTheme.colorScheme.error
                call.type == CallLog.Calls.MISSED_TYPE -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.onSurface
            },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f, fill = false)
        )

        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.padding(start = 8.dp)
        ) {
            if (timeOnly) {
                if (showTime) {
                    Text(
                        text = timeText,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            } else {
                Text(
                    text = dateText,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (showTime) {
                    Text(
                        text = timeText,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}
