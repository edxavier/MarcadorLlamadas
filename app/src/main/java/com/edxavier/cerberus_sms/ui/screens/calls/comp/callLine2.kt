package com.edxavier.cerberus_sms.ui.screens.calls.comp

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.edxavier.cerberus_sms.R
import com.edxavier.cerberus_sms.data.models.CallsLog
import com.edxavier.cerberus_sms.helpers.timeFormat
import com.edxavier.cerberus_sms.ui.screens.calls.CallDate

@Composable
fun CallLine2(call:CallsLog) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        if( call.sim > 0){
            val icon = painterResource(id = R.drawable.sim_card)
            Image(
                painter = icon,
                contentDescription = null, modifier = Modifier.size(12.dp),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.outline)
            )
            Text(text = "${call.sim}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Light)
            Spacer(modifier = Modifier.width(4.dp))
        }
        Divider(
            modifier = Modifier
                .height(12.dp)  //fill the max height
                .width(1.dp),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = "[${call.total}]", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Light)
        Spacer(modifier = Modifier.width(4.dp))
        val icon = painterResource(id = R.drawable.baseline_access_time_24)
        Image(
            painter = icon,
            contentDescription = null, modifier = Modifier.size(14.dp),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.outline)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = call.duration.timeFormat(), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Light)
        Spacer(modifier = Modifier.width(4.dp))
        Divider(
            modifier = Modifier
                .height(12.dp)  //fill the max height
                .width(1.dp),
        )
        Spacer(modifier = Modifier.width(4.dp))
        CallDate(call = call)

    }
}