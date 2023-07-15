package com.edxavier.cerberus_sms.ui.screens.calls.comp

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
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
    Card(modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp)) {
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .weight(1f)
                    .clickable { onDelete() },
            ){
                Spacer(Modifier.height(8.dp))
                Icon(imageVector = Icons.Outlined.Delete, contentDescription = null)
                Text(text="Eliminar", style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.height(8.dp))
            }

            Divider(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .height(52.dp)  //fill the max height
                    .width(1.dp),
                color = MaterialTheme.colorScheme.outline
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .weight(1f)
                    .clickable { onText() },
            ){
                Spacer(Modifier.height(8.dp))
                Icon(imageVector = ImageVector.vectorResource(id = R.drawable.chat), contentDescription = null)
                Text(text="Escribir", style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.height(8.dp))
            }
            Divider(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .height(52.dp)  //fill the max height
                    .width(1.dp),
                color = MaterialTheme.colorScheme.outline
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .weight(1f)
                    .clickable { onCall() },
            ){
                Spacer(Modifier.height(8.dp))
                Icon(imageVector = Icons.Outlined.Call, contentDescription = null)
                Text(text="LLamar", style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.height(8.dp))
            }
            Divider(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .height(52.dp)  //fill the max height
                    .width(1.dp),
                color = MaterialTheme.colorScheme.outline
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .weight(1f)
                    .clickable {
                        onBlock()
                    },
            ){
                Spacer(Modifier.height(8.dp))
                if(isNumberBlocked) {
                    Icon(imageVector = ImageVector.vectorResource(id = R.drawable.lock_on), contentDescription = null)
                    Text(text="Desloquear", style = MaterialTheme.typography.labelSmall)
                }else{
                    Icon(imageVector = ImageVector.vectorResource(id = R.drawable.lock_off), contentDescription = null)
                    Text(text="Bloquear", style = MaterialTheme.typography.labelSmall)
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}