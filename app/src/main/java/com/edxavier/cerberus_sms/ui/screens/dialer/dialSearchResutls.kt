package com.edxavier.cerberus_sms.ui.screens.dialer

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.edxavier.cerberus_sms.helpers.makeCall
import com.edxavier.cerberus_sms.ui.calls.AppViewModel
import com.edxavier.cerberus_sms.ui.screens.calls.CallLogEntry
import com.edxavier.cerberus_sms.ui.screens.contacts.ContactEntry

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DialSearchResults(
    viewModel: AppViewModel,
    navController: NavHostController
) {
    val state by viewModel.uiState.collectAsState()
    val mContext = LocalContext.current
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 2.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ){
        if(state.dialCalls.isNotEmpty()) {
            stickyHeader {
                Text(
                    text = "LLamadas",
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface)
                        .clip(RoundedCornerShape(2.dp))
                        .padding(4.dp).fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
                )
            }
            items(items = state.dialCalls) {
                CallLogEntry(call = it, viewModel = viewModel, navCtrl = navController)
            }
        }
        stickyHeader {
            Text(
                text = "Contactos",
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .clip(RoundedCornerShape(2.dp))
                    .padding(4.dp).fillMaxWidth(),
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp
            )
        }
        items(items = state.dialContacts){
            ContactEntry(contact = it, onClick = {
                mContext.makeCall(it.number)
            })
        }
    }
}