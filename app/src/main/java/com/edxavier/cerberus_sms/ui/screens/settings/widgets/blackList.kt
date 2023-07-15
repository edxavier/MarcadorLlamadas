package com.edxavier.cerberus_sms.ui.screens.settings.widgets

import android.content.Context
import android.os.Build
import android.telecom.TelecomManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edxavier.cerberus_sms.R

@Composable
fun BlackList() {
    val myContext = LocalContext.current
    val telecomManager = myContext.getSystemService(Context.TELECOM_SERVICE) as TelecomManager

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                myContext.startActivity(telecomManager.createManageBlockedNumbersIntent(), null)
            }
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        Icon(imageVector = Icons.Default.Lock, contentDescription = null)
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "Numeros Bloqueados", modifier = Modifier.weight(1f),
            fontSize = 16.sp, fontWeight = FontWeight.Light
        )
        Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = null)
    }
}