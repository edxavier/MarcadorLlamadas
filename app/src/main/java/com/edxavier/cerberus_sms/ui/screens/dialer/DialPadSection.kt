package com.edxavier.cerberus_sms.ui.screens.dialer

import android.widget.Toast
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edxavier.cerberus_sms.R
import com.edxavier.cerberus_sms.data.models.Operator
import com.edxavier.cerberus_sms.data.repositories.RepoOperator
import com.edxavier.cerberus_sms.helpers.AnalyticsLogger
import com.edxavier.cerberus_sms.helpers.getOperatorColor
import com.edxavier.cerberus_sms.helpers.getOperatorString
import com.edxavier.cerberus_sms.helpers.makeCall
import com.edxavier.cerberus_sms.helpers.sendSms
import com.edxavier.cerberus_sms.ui.calls.AppViewModel
import kotlinx.coroutines.launch

@Composable
fun DialPadSection(
    dialNumber: String,
    onDialNumberChange: (String) -> Unit,
    cursorPos: Int,
    onCursorPosChange: (Int) -> Unit,
    onHidePad: () -> Unit,
    viewModel: AppViewModel
) {
    val context = LocalContext.current
    var operator: Operator? by remember { mutableStateOf(Operator()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { AnalyticsLogger.dialerOpened() }

    LaunchedEffect(dialNumber) {
        val opRepo = RepoOperator(context)
        operator = opRepo.getOperator(dialNumber)
    }

    Surface(
        tonalElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        shadowElevation = 16.dp
    ) {
        var dragAccumulator by remember { mutableFloatStateOf(0f) }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragEnd = {
                            if (dragAccumulator > 800f) onHidePad()
                            dragAccumulator = 0f
                        },
                        onVerticalDrag = { _, dragAmount ->
                            dragAccumulator += dragAmount
                        }
                    )
                }
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                HorizontalDivider(
                    modifier = Modifier.width(36.dp),
                    thickness = 3.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Operator badge — tint sutil con color del operador
            operator?.let { op ->
                if (!op.operator.getOperatorString().startsWith("INTE")) {
                    val opStr = if (op.operator.getOperatorString().startsWith("LINEA")) {
                        "${op.area} ${op.country}"
                    } else {
                        op.operator.getOperatorString().replaceFirstChar { c -> c.uppercase() }
                    }
                    val opColor = Color(op.operator.getOperatorColor(context))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = opColor.copy(alpha = 0.50f),
                        tonalElevation = 2.dp
                    ) {
                        Text(
                            text = opStr,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            NumberInput(
                valueText = dialNumber,
                cursorPos = cursorPos,
                onBackSpace = { number, cursor ->
                    onDialNumberChange(number)
                    onCursorPosChange(cursor)
                    if (number.isNotEmpty()) {
                        scope.launch { viewModel.getDialRecords(number) }
                    } else {
                        scope.launch { viewModel.setEmptyDialSearch() }
                    }
                },
                onCursorPosChange = { cPos ->
                    onCursorPosChange(cPos)
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            val dialKeys = listOf(
                KeyContent("1", ""),
                KeyContent("2", "ABC"),
                KeyContent("3", "DEF"),
                KeyContent("4", "GHI"),
                KeyContent("5", "JKL"),
                KeyContent("6", "MNO"),
                KeyContent("7", "PQRS"),
                KeyContent("8", "TUV"),
                KeyContent("9", "WXYZ"),
                KeyContent("*", ""),
                KeyContent("0", "+"),
                KeyContent("#", ""),
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(dialKeys) { kContent ->
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        DialKeyboard(
                            mainText = kContent.mainText,
                            secondaryText = kContent.secText,
                            keySize = 52.dp,
                            fontSize = 22.sp,
                            onKeyPress = { char ->
                                val newNumber = StringBuilder(dialNumber).apply {
                                    if (cursorPos >= 0) {
                                        insert(cursorPos, char)
                                        onCursorPosChange(cursorPos + 1)
                                    } else {
                                        insert(dialNumber.length, char)
                                    }
                                }.toString()
                                onDialNumberChange(newNumber)
                                scope.launch { viewModel.getDialRecords(newNumber) }
                            },
                            enableLongKeyPress = kContent.mainText == "0"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    FilledTonalButton(
                        onClick = {
                            if (dialNumber.isNotEmpty())
                                context.sendSms(dialNumber)
                            else
                                Toast.makeText(context, "Digite un numero", Toast.LENGTH_LONG).show()
                        },
                        modifier = Modifier.size(width = 80.dp, height = 48.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.chat),
                            contentDescription = "Enviar SMS",
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "SMS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                FloatingActionButton(
                    onClick = {
                        if (dialNumber.isNotEmpty()) {
                            context.makeCall(dialNumber)
                            AnalyticsLogger.callOutgoing()
                        } else
                            Toast.makeText(context, "Digite un numero", Toast.LENGTH_LONG).show()
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_call_24),
                        contentDescription = "Llamar",
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    FilledTonalButton(
                        onClick = onHidePad,
                        modifier = Modifier.size(width = 80.dp, height = 48.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_baseline_dialpad),
                            contentDescription = "Ocultar teclado",
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Cerrar",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
