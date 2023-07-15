package com.edxavier.cerberus_sms.ui.screens.settings

import android.app.role.RoleManager
import android.content.Intent
import android.provider.Telephony
import android.telecom.TelecomManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.edxavier.cerberus_sms.navigation.Routes
import com.edxavier.cerberus_sms.ui.calls.AppViewModel
import com.edxavier.cerberus_sms.ui.screens.settings.widgets.BlackList
import com.edxavier.cerberus_sms.ui.screens.settings.widgets.CleanCallLogMenu
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, viewModel: AppViewModel) {
   val myContext = LocalContext.current
    val scope = rememberCoroutineScope()
    Scaffold(
        topBar = {
            TopAppBar(
                title ={
                    Text(text = "Ajustes", softWrap = false, overflow = TextOverflow.Ellipsis)
                },
            )
        },
    ) {
        LazyColumn(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues = it),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ){
            item{
                BlackList()
            }
            item{
                CleanCallLogMenu(onCleanCallLog = {
                    scope.launch {
                        viewModel.deleteCallsForNumber()
                    }
                    Toast.makeText(myContext, "Su registro de llamadas fue eliminado", Toast.LENGTH_LONG).show()
                })
            }
        }
    }

}