package com.edxavier.cerberus_sms.ui.screens.incall

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.edxavier.cerberus_sms.ui.core.ui.MyBannerAd
import com.edxavier.cerberus_sms.ui.core.ui.NativeMediumAd
import com.edxavier.cerberus_sms.ui.ui.theme.white_50

@Composable
fun OnGoingCalls(
    viewModel: InCallViewModel
) {
    val state by viewModel.uiState.collectAsState()
    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        item {
            if((0..1).random() == 0)
                NativeMediumAd()
            else
                MyBannerAd()
        }
        if(state.callsQueue.size==1){
            item {
                SingleOngoingCall(call = state.callsQueue.first())
            }
        }else{
            items(items = state.callsQueue){
                MultiOngoingCall(call = it)
                Spacer(modifier = Modifier.height(4.dp))
                Divider(
                    Modifier
                        .background(white_50)
                        .height(1.dp)
                        .fillMaxWidth()
                        .padding(8.dp))
            }
        }
    }
}