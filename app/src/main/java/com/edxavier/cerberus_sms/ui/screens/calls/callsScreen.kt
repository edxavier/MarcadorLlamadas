package com.edxavier.cerberus_sms.ui.screens.calls

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.NavHostController
import com.edxavier.cerberus_sms.R
import com.edxavier.cerberus_sms.data.models.Operator
import com.edxavier.cerberus_sms.data.repositories.RepoOperator
import com.edxavier.cerberus_sms.helpers.makeCall
import com.edxavier.cerberus_sms.helpers.sendSms
import com.edxavier.cerberus_sms.ui.calls.AppViewModel
import com.edxavier.cerberus_sms.ui.core.ui.MyBannerAd
import com.edxavier.cerberus_sms.ui.core.ui.NativeMediumAd
import com.edxavier.cerberus_sms.ui.screens.calls.comp.NoDataScreen
import com.edxavier.cerberus_sms.ui.screens.dialer.DialKeyboard
import com.edxavier.cerberus_sms.ui.screens.dialer.KeyContent
import com.edxavier.cerberus_sms.ui.screens.dialer.NumberInput
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallLogScreen(
    viewModel:AppViewModel,
    navCtrl: NavHostController
) {
    val myContext = LocalContext.current
    val state by viewModel.uiState.collectAsState()
    var showPad by remember { mutableStateOf(state.dialShown) }
    var dialNumber by remember { mutableStateOf(state.dialNumber) }
    var cursorPos by remember { mutableStateOf(-1) }
    var operator: Operator? by remember { mutableStateOf(Operator()) }
    val listState = rememberLazyListState()
    val expandedFabState = remember {
        derivedStateOf {
            listState.firstVisibleItemScrollOffset
        }
    }
    LaunchedEffect(key1 = expandedFabState.value) {
        viewModel.expandedFab = viewModel.firstVisible >= expandedFabState.value
        viewModel.firstVisible = expandedFabState.value
    }

    LaunchedEffect(true){
        /*
        if(myContext.hasRequiredPermissions()) {
            viewModel.getCallLog()
        }
        */
    }

    Scaffold(
        floatingActionButton = {
            AnimatedVisibility(
                visible = (!showPad && viewModel.expandedFab),
                enter = scaleIn(animationSpec = tween(durationMillis = 500)),
                exit = scaleOut(animationSpec = tween(durationMillis = 500))
            ) {
                FloatingActionButton(onClick = {
                    showPad = !showPad
                    viewModel.onDialPadEvent()
                }) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_baseline_dialpad),
                        contentDescription = null
                    )
                }
            }
        }
    ) {
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(it)
        ) {
            NativeMediumAd()

            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                NoDataScreen(
                    message = "El registro de llamadas esta restringido por politicas de Google en esta version de la app, si deseas tener la version completa," +
                            " descarga el APK e instalalo de manera manual",
                    imageId = R.drawable.recent_calls
                )
                Button(onClick = {
                    val url = "https://verificaoperadora.edxr.es/descargas/app-release.apk"
                    val i = Intent(Intent.ACTION_VIEW)
                    i.data = Uri.parse(url)
                    myContext.startActivity(i)
                }) {
                    Text(text = "Descargar APK ")
                }
            }

            AnimatedVisibility(
                visible = (showPad),
                modifier = Modifier
                    .align(Alignment.BottomStart),
                enter = slideInVertically(animationSpec = tween(delayMillis = 300, durationMillis = 250)) { fullHeight ->
                    fullHeight
                },
                exit = slideOutVertically(animationSpec = tween(durationMillis = 200)) { fullHeight ->
                    fullHeight
                }
            ) {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 12.dp,
                        bottomEnd = 0.dp,
                        bottomStart = 0.dp
                    ),
                ) {
                    val opRepo = RepoOperator(LocalContext.current)
                    val scope = rememberCoroutineScope()
                    scope.launch {
                        operator = opRepo.getOperator(dialNumber)
                    }
                    Column(
                        modifier = Modifier
                            .padding(12.dp)
                    ) {
                        NumberInput(
                            valueText = dialNumber,
                            cursorPos = cursorPos,
                            operator = operator,
                            onBackSpace = { number, cursor ->
                                dialNumber = number
                                cursorPos = cursor
                                if(dialNumber.isNotEmpty()) {
                                    scope.launch {
                                        // viewModel.getDialRecords(dialNumber)
                                    }
                                }else{
                                    scope.launch {
                                        viewModel.setEmptyDialSearch()
                                    }
                                }
                            },
                            onCursorPosChange = {cPos->
                                cursorPos = cPos
                            }
                        )
                        Divider(modifier = Modifier
                            .height(1.dp)
                            .fillMaxWidth()
                        )

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
                            contentPadding = PaddingValues(12.dp),
                            verticalArrangement = Arrangement.Center
                        ){
                            items(dialKeys){kContent->
                                val longPress = kContent.mainText == "0"
                                DialKeyboard(
                                    kContent.mainText,
                                    kContent.secText,
                                    onKeyPress = { char ->
                                        dialNumber = StringBuilder(dialNumber)
                                            .apply {
                                                if(cursorPos>=0) {
                                                    insert(cursorPos, char)
                                                    cursorPos+=1
                                                }else{
                                                    insert(dialNumber.length, char)
                                                }
                                            }.toString()
                                        scope.launch {
                                                // viewModel.getDialRecords(dialNumber)
                                        }
                                    },
                                    enableLongKeyPress = longPress
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceAround,
                            modifier = Modifier.fillMaxWidth()
                        ){
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.chat),
                                contentDescription = null,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        if (dialNumber.isNotEmpty())
                                            myContext.sendSms(dialNumber)
                                        else
                                            Toast
                                                .makeText(
                                                    myContext,
                                                    "Digite un numero",
                                                    Toast.LENGTH_LONG
                                                )
                                                .show()
                                    }
                                    .padding(12.dp)
                            )
                            SmallFloatingActionButton(onClick = {
                                if(dialNumber.isNotEmpty())
                                    myContext.makeCall(dialNumber)
                                else
                                    Toast.makeText(myContext, "Digite un numero", Toast.LENGTH_LONG).show()
                            }) {
                                Icon(
                                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_call_24),
                                    contentDescription = null
                                )
                            }
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.ic_baseline_dialpad),
                                contentDescription = null,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        showPad = !showPad
                                        viewModel.onDialPadEvent()
                                    }
                                    .padding(8.dp)
                            )

                        }
                    }
                }
            }
        }
    }
}