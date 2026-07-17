package com.edxavier.cerberus_sms.ui.screens.calls.comp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.edxavier.cerberus_sms.R

@Composable
fun OptionsHistory(
    onCall: () -> Unit,
    onText: () -> Unit,
    onDelete: () -> Unit,
    onBlock: () -> Unit,
    isNumberBlocked:Boolean = false
) {
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        OptionsHistoryChip(
            icon = { Icon(imageVector = Icons.Outlined.Delete, contentDescription = "Eliminar") },
            label = "Eliminar",
            onClick = onDelete
        )
        OptionsHistoryChip(
            icon = { Icon(painter = painterResource(id = R.drawable.chat), contentDescription = "Escribir") },
            label = "Escribir",
            onClick = onText
        )
        OptionsHistoryChip(
            icon = { Icon(imageVector = Icons.Outlined.Call, contentDescription = "Llamar") },
            label = "Llamar",
            onClick = onCall
        )
        OptionsHistoryChip(
            icon = {
                if(isNumberBlocked) {
                    Icon(painter = painterResource(id = R.drawable.lock_on), contentDescription = "Desbloquear")
                }else{
                    Icon(painter = painterResource(id = R.drawable.lock_off), contentDescription = "Bloquear")
                }
            },
            label = if(isNumberBlocked) "Desbloquear" else "Bloquear",
            onClick = onBlock
        )
    }
}

@Composable
private fun OptionsHistoryChip(
    icon: @Composable () -> Unit,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .width(80.dp)
                .padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(modifier = Modifier.padding(6.dp)) {
                icon()
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
