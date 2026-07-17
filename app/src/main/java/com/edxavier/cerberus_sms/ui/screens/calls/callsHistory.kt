package com.edxavier.cerberus_sms.ui.screens.calls

import android.provider.BlockedNumberContract
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.edxavier.cerberus_sms.helpers.FlowEventBus
import com.edxavier.cerberus_sms.helpers.makeCall
import com.edxavier.cerberus_sms.helpers.sendSms
import com.edxavier.cerberus_sms.ui.calls.AppViewModel
import com.edxavier.cerberus_sms.ui.core.ui.ConfirmDialog
import com.edxavier.cerberus_sms.ui.core.ui.LoadingIndicator
import com.edxavier.cerberus_sms.ui.screens.calls.comp.OptionsHistory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CallHistory(viewModel:AppViewModel, navController: NavHostController) {
    val myContext = LocalContext.current
    val state by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val call = viewModel.selectedCall
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    var confirm by remember { mutableStateOf(false) }
    var locked by remember { mutableStateOf(call.isBlocked) }
    if(confirm){
        ConfirmDialog(
            title = "Continuar?",
            content = buildAnnotatedString {
                append("Se eleminara el historial de llamadas de ")
                withStyle(
                    style = SpanStyle(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    append(call.name)
                }
            },
            onConfirm = {
                confirm = false
                scope.launch {
                    viewModel.deleteCallsForNumber(call.number)
                    viewModel.getCallsFor(call.number)
                }
            },
            onDismiss = { confirm = false },
            onCancel = { confirm = false }
        )
    }
    scope.launch {
        FlowEventBus.subscribe<String> {
            scope.launch { viewModel.getCallsFor(call.number) }
        }
    }
    LaunchedEffect(true){
        viewModel.getCallsFor(call.number)
    }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title ={
                    Text(text =  call.name, softWrap = false, overflow = TextOverflow.Ellipsis)
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigateUp()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) {
        if(state.isLoading){
            LoadingIndicator()
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ){
            stickyHeader {
                OptionsHistory(
                    onCall = { myContext.makeCall(call.number) },
                    onText = { myContext.sendSms(call.number) },
                    onDelete = { confirm = true},
                    onBlock={
                        if(locked){
                            BlockedNumberContract.unblock(myContext, call.number)
                            Toast.makeText(myContext, "${call.name} desbloqueado", Toast.LENGTH_LONG).show()
                        }else{
                            viewModel.blockNumber(call.number)
                            Toast.makeText(myContext, "${call.name} bloqueado", Toast.LENGTH_LONG).show()
                        }
                        locked = !locked
                    },
                    isNumberBlocked = locked
                )
            }
            items(items = state.callLogForNumber, key = { cl -> cl.id }){ callLog ->
                HistoryLogEntry(call = callLog)
            }
        }

    }
}