package com.edxavier.cerberus_sms.ui.screens.calls.comp

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
    Surface(
        tonalElevation = 2.dp,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            OptionsHistoryItem(
                icon = { Icon(imageVector = Icons.Outlined.Delete, contentDescription = "Eliminar") },
                label = "Eliminar",
                onClick = onDelete
            )
            OptionsHistoryItem(
                icon = { Icon(painter = painterResource(id = R.drawable.chat), contentDescription = "Escribir") },
                label = "Escribir",
                onClick = onText
            )
            OptionsHistoryItem(
                icon = { Icon(imageVector = Icons.Outlined.Call, contentDescription = "Llamar") },
                label = "Llamar",
                onClick = onCall
            )
            OptionsHistoryItem(
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
}

@Composable
private fun OptionsHistoryItem(
    icon: @Composable () -> Unit,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .size(width = 72.dp, height = 56.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            tonalElevation = 1.dp,
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(modifier = Modifier.padding(10.dp)) {
                icon()
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}