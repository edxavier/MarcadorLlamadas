package com.edxavier.cerberus_sms.ui.screens.settings.widgets

import android.content.Context
import android.os.Build
import android.telecom.TelecomManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edxavier.cerberus_sms.R
import com.edxavier.cerberus_sms.ui.core.ui.ConfirmDialog
import kotlinx.coroutines.launch

@Composable
fun CleanCallLogMenu(
    onCleanCallLog: ()->Unit
) {
    val myContext = LocalContext.current
    var confirm by remember { mutableStateOf(false) }
    val content = buildAnnotatedString {
        append("Se eleminara todo el historial de llamadas")
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                confirm = true
            }
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        Icon(imageVector = Icons.Default.Clear, contentDescription = null)
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "Eliminar todas las llamadas", modifier = Modifier.weight(1f),
            fontSize = 16.sp, fontWeight = FontWeight.Light
        )
        Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = null)
    }
    if(confirm){
        ConfirmDialog(
            title = "Continuar?",
            content = content,
            onConfirm = {
                confirm = false
                onCleanCallLog()
            },
            onDismiss = { confirm = false },
            onCancel = {confirm = false}
        )
    }
}