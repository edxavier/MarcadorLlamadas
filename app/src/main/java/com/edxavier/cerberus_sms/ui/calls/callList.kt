package com.edxavier.cerberus_sms.ui.calls

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.edxavier.cerberus_sms.data.models.CallsLog
import com.edxavier.cerberus_sms.ui.core.ui.MyBannerAd
import com.edxavier.cerberus_sms.ui.screens.calls.CallLogEntry

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CallsScreen(
    callLog: List<CallsLog>,
    navCtrl: NavHostController,
    viewModel: AppViewModel,
    listState: LazyListState
) {

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 0.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        state = listState,
    ){
        item {
            MyBannerAd()
        }
        itemsIndexed(items = callLog, key = { _, item -> item.id }){ index, call ->
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(delayMillis = index * 50, durationMillis = 300)) +
                        slideInVertically(
                            animationSpec = tween(delayMillis = index * 50, durationMillis = 300)
                        ) { it / 4 }
            ) {
                CallLogEntry(call = call, navCtrl = navCtrl, viewModel = viewModel)
            }
        }
    }
}