package com.edxavier.cerberus_sms.ui.screens.calls

import android.provider.BlockedNumberContract
import android.provider.CallLog
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.edxavier.cerberus_sms.R
import com.edxavier.cerberus_sms.data.models.CallsLog
import com.edxavier.cerberus_sms.helpers.getCallDirectionIcon
import com.edxavier.cerberus_sms.helpers.getOperatorColor
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
        append("Se eliminara el historial de llamadas de ")
        withStyle(
            style = SpanStyle(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
        ) { append(call.name) }
    }

    val operatorColor = call.operator?.let {
        Color(it.operator.getOperatorColor(context))
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .padding(horizontal = 12.dp)
            .combinedClickable(
                onClick = { showMenu = !showMenu },
                onLongClick = { expanded = !expanded }
            ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max)
        ) {
            // Operator color left border — solo cuando hay operador
            if (operatorColor != null) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
                        .background(operatorColor)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 3.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 12.dp, top = 14.dp, bottom = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val filter = if (call.type == CallLog.Calls.MISSED_TYPE || call.type == CallLog.Calls.BLOCKED_TYPE)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.secondary

                    val items = mutableListOf("Eliminar historial")

                    if (expanded) {
                        MenuDialog(
                            title = call.name,
                            options = items,
                            onItemClick = {
                                expanded = false
                                when (it) {
                                    0 -> {
                                        if (call.isBlocked) {
                                            BlockedNumberContract.unblock(context, call.number)
                                            Toast.makeText(context, "${call.name} desbloqueado", Toast.LENGTH_LONG).show()
                                        } else {
                                            viewModel.blockNumber(call.number)
                                            Toast.makeText(context, "${call.name} bloqueado", Toast.LENGTH_LONG).show()
                                        }
                                        scope.launch { viewModel.getCallLog() }
                                    }
                                }
                            },
                            onDismiss = { expanded = false }
                        )
                    }

                    if (confirm) {
                        ConfirmDialog(
                            title = "Continuar?",
                            content = content,
                            onConfirm = {
                                confirm = false
                                viewModel.deleteCallsForNumber(call.number)
                                scope.launch { viewModel.getCallLog() }
                            },
                            onDismiss = { confirm = false },
                            onCancel = { confirm = false }
                        )
                    }

                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = filter.copy(alpha = 0.15f),
                        tonalElevation = 2.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(id = call.type.getCallDirectionIcon()),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = filter
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        CallLine1(call = call)
                        Spacer(modifier = Modifier.height(4.dp))
                        CallLine2(call = call)
                        Spacer(modifier = Modifier.height(2.dp))
                        CallLine3(call = call)
                    }
                }

            AnimatedVisibility(
                visible = showMenu,
                enter = fadeIn(animationSpec = tween(200)) + expandVertically(animationSpec = tween(200)),
                exit = fadeOut(animationSpec = tween(150)) + shrinkVertically(animationSpec = tween(150))
            ) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ActionButton(
                            icon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.chat),
                                    contentDescription = "Enviar SMS",
                                    tint = MaterialTheme.colorScheme.tertiary
                                )
                            },
                            onClick = { context.sendSms(call.number) }
                        )
                        ActionButton(
                            icon = {
                                Icon(
                                    imageVector = Icons.Outlined.Info,
                                    contentDescription = "Historial",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            onClick = {
                                viewModel.selectedCall = call
                                navCtrl.navigate(Routes.CallHistory.route)
                            }
                        )
                        ActionButton(
                            icon = {
                                Icon(
                                    imageVector = Icons.Outlined.Phone,
                                    contentDescription = "Llamar",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            onClick = { context.makeCall(call.number) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionButton(
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        tonalElevation = 1.dp
    ) {
        Box(
            modifier = Modifier.padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }
    }
}
