package com.edxavier.cerberus_sms.ui.screens.incall

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.edxavier.cerberus_sms.ui.core.ui.MyBannerAd
import com.edxavier.cerberus_sms.ui.core.ui.NativeMediumAd

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
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    color = Color.White.copy(alpha = 0.12f),
                    thickness = 1.dp
                )
            }
        }
    }
}