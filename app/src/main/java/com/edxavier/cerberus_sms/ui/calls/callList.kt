package com.edxavier.cerberus_sms.ui.calls

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.edxavier.cerberus_sms.data.models.CallsLog
import com.edxavier.cerberus_sms.ui.core.ui.MyBannerAd
import com.edxavier.cerberus_sms.ui.core.ui.NativeMediumAd
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
            .padding(horizontal = 2.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        state = listState,
    ){
        item {
            MyBannerAd()
        }
        items(items = callLog, key = { it.id }){
            CallLogEntry(call = it, navCtrl = navCtrl, viewModel = viewModel)
        }
    }
}