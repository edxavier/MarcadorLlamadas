package com.edxavier.cerberus_sms.ui.screens.calls

import android.provider.BlockedNumberContract
import android.provider.CallLog
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.edxavier.cerberus_sms.R
import com.edxavier.cerberus_sms.data.models.CallsLog
import com.edxavier.cerberus_sms.helpers.getCallDirectionIcon
import com.edxavier.cerberus_sms.helpers.makeCall
import com.edxavier.cerberus_sms.helpers.sendSms
import com.edxavier.cerberus_sms.navigation.Routes
import com.edxavier.cerberus_sms.ui.calls.AppViewModel
import com.edxavier.cerberus_sms.ui.core.ui.ConfirmDialog
import com.edxavier.cerberus_sms.ui.core.ui.MenuDialog
import com.edxavier.cerberus_sms.ui.screens.calls.comp.CallLine1
import com.edxavier.cerberus_sms.ui.screens.calls.comp.CallLine2
import com.edxavier.cerberus_sms.ui.screens.calls.comp.CallLine3
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CallLogEntry(
    call: CallsLog,
    navCtrl: NavHostController,
    viewModel: AppViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var expanded by remember { mutableStateOf(false) }
    var confirm by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }



    val content = buildAnnotatedString {
        append("Se eleminara el historial de llamadas de ")
        withStyle(
            style = SpanStyle(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
        ) {append(call.name) }
    }
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation =  CardDefaults.cardElevation(1.dp),
        modifier = Modifier
            .padding(horizontal = 6.dp)
            .combinedClickable(
                onClick = { showMenu = !showMenu},
                onLongClick = {
                    expanded = !expanded
                }
            ),
    ){
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(0.dp),
                verticalAlignment = Alignment.CenterVertically
            ){
                val filter = if(call.type == CallLog.Calls.MISSED_TYPE || call.type == CallLog.Calls.BLOCKED_TYPE)
                    Color.Red
                else
                    MaterialTheme.colorScheme.secondary
                val items = mutableListOf("Eliminar historial")

                if (expanded) {
                    MenuDialog(
                        title = call.name,
                        options = items,
                        onItemClick = {
                            expanded = false
                            when(it){
                                0 -> {
                                    if(call.isBlocked){
                                        BlockedNumberContract.unblock(context, call.number)
                                        Toast.makeText(context, "${call.name} desbloqueado", Toast.LENGTH_LONG).show()
                                    }else{
                                        viewModel.blockNumber(call.number)
                                        Toast.makeText(context, "${call.name} bloqueado", Toast.LENGTH_LONG).show()
                                    }
                                    scope.launch { viewModel.getCallLog() }
                                }
                            }
                        },
                        onDismiss = { expanded = false}
                    )
                }

                if(confirm){
                    ConfirmDialog(
                        title = "Continuar?",
                        content = content,
                        onConfirm = {
                            confirm = false
                            viewModel.deleteCallsForNumber(call.number)
                            scope.launch { viewModel.getCallLog() }
                        },
                        onDismiss = { confirm = false },
                        onCancel = {confirm = false}
                    )
                }
                Image(
                    painter = painterResource(id = call.type.getCallDirectionIcon()),
                    contentDescription = null, modifier = Modifier.size(18.dp),
                    colorFilter = ColorFilter.tint(filter),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)){
                    CallLine1(call = call)
                    CallLine2(call = call)
                    Spacer(modifier = Modifier.height(2.dp))
                    CallLine3(call = call)
                }
            }

            AnimatedVisibility(visible = (showMenu)) {
                Row(
                    Modifier
                        .padding(2.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    var locked by remember {
                        mutableStateOf(BlockedNumberContract.isBlocked(context, call.number))
                    }
                    val icon = if(locked) ImageVector.vectorResource(
                        id = R.drawable.lock_on
                    )else ImageVector.vectorResource(
                        id = R.drawable.lock_off
                    )
                    /*
                    Icon(
                        imageVector = icon, contentDescription = null,
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable {
                                locked = !locked
                                if(call.isBlocked){
                                    BlockedNumberContract.unblock(context, call.number)
                                    Toast.makeText(context, "${call.name} desbloqueado", Toast.LENGTH_LONG).show()
                                }else{
                                    viewModel.blockNumber(call.number)
                                    Toast.makeText(context, "${call.name} bloqueado", Toast.LENGTH_LONG).show()
                                }
                                scope.launch { viewModel.getCallLog() }
                            }
                            .padding(14.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                     */
                    Icon(
                        imageVector = ImageVector.vectorResource(
                            id = R.drawable.chat
                        ),
                        contentDescription = null,
                        modifier = Modifier
                            .clip(CircleShape)
                            
                            .clickable {
                                context.sendSms(call.number)
                            }
                            .padding(14.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        modifier = Modifier
                            .clip(CircleShape)
                            
                            .clickable {
                                viewModel.selectedCall = call
                                navCtrl.navigate(Routes.CallHistory.route)
                            }
                            .padding(14.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(
                        imageVector = Icons.Outlined.Phone,
                        contentDescription = null,
                        modifier = Modifier
                            .clip(CircleShape)
                            
                            .clickable { context.makeCall(call.number) }
                            .padding(14.dp)
                    )
                }
            }
        }
    }





}