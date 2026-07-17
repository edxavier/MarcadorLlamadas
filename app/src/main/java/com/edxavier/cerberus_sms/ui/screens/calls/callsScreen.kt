package com.edxavier.cerberus_sms.ui.screens.calls

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.edxavier.cerberus_sms.R
import com.edxavier.cerberus_sms.ui.calls.CallsScreen
import com.edxavier.cerberus_sms.ui.calls.AppViewModel
import com.edxavier.cerberus_sms.ui.core.ui.CallLogSkeleton
import com.edxavier.cerberus_sms.ui.screens.calls.comp.NoDataScreen
import com.edxavier.cerberus_sms.ui.screens.dialer.DialPadSection
import com.edxavier.cerberus_sms.ui.screens.dialer.DialSearchResults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallLogScreen(
    viewModel: AppViewModel,
    navCtrl: NavHostController
) {
    val state by viewModel.uiState.collectAsState()
    var showPad by remember { mutableStateOf(state.dialShown) }
    var dialNumber by remember { mutableStateOf(state.dialNumber) }
    var cursorPos by remember { mutableStateOf(-1) }
    val listState = rememberLazyListState()

    LaunchedEffect(true) {
        viewModel.getCallLog()
    }

    BackHandler(enabled = showPad) {
        showPad = false
        viewModel.onDialPadEvent()
    }

    Scaffold {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            when {
                state.isLoading && state.callLog.isEmpty() -> {
                    CallLogSkeleton()
                }
                !state.isLoading && state.callLog.isEmpty() -> {
                    NoDataScreen(
                        message = "Tu historial de llamadas está vacío",
                        subtitle = "Las llamadas aparecerán aquí",
                        imageId = R.drawable.recent_calls,
                        onAction = {
                            showPad = !showPad
                            viewModel.onDialPadEvent()
                        }
                    )
                }
                else -> {
                    if (state.dialCalls.isEmpty() && state.dialContacts.isEmpty()) {
                        CallsScreen(
                            callLog = state.callLog,
                            navCtrl = navCtrl,
                            viewModel = viewModel,
                            listState = listState
                        )
                    } else {
                        DialSearchResults(viewModel = viewModel, navController = navCtrl)
                    }
                }
            }

            // FAB siempre visible
            if (!showPad) {
                FloatingActionButton(
                    onClick = {
                        showPad = !showPad
                        viewModel.onDialPadEvent()
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 96.dp)
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_baseline_dialpad),
                        contentDescription = "Abrir marcador"
                    )
                }
            }

            // Scrim overlay — tocar fuera del dial pad lo cierra
            if (showPad) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            showPad = false
                            viewModel.onDialPadEvent()
                        }
                )
            }

            AnimatedVisibility(
                visible = showPad,
                modifier = Modifier.align(Alignment.BottomCenter),
                enter = slideInVertically(
                    animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f)
                ) { it },
                exit = slideOutVertically(
                    animationSpec = tween(durationMillis = 200)
                ) { it }
            ) {
                DialPadSection(
                    dialNumber = dialNumber,
                    onDialNumberChange = { dialNumber = it },
                    cursorPos = cursorPos,
                    onCursorPosChange = { cursorPos = it },
                    onHidePad = {
                        showPad = !showPad
                        viewModel.onDialPadEvent()
                    },
                    viewModel = viewModel
                )
            }
        }
    }
}