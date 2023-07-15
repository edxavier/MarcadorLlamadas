package com.edxavier.cerberus_sms.ui.screens.incall

import android.app.NotificationManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.getSystemService
import com.edxavier.cerberus_sms.R
import com.edxavier.cerberus_sms.helpers.MyCallsManager
import com.edxavier.cerberus_sms.ui.ui.theme.green_700
import com.edxavier.cerberus_sms.ui.ui.theme.red_700
import com.edxavier.cerberus_sms.ui.ui.theme.white_50

@Composable
fun CallMainButtons(
    viewModel: InCallViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val ctx = LocalContext.current
    val infiniteTransition = rememberInfiniteTransition()

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(200),
            repeatMode = RepeatMode.Reverse
        )
    )

    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        val inactiveBtnModifier = Modifier
            .clip(CircleShape)
            .background(Color.Transparent, CircleShape)
        val activeBtnModifier = Modifier
            .clip(CircleShape)
            .background(white_50, CircleShape)
        val micIcon = if(!state.micOff) ImageVector.vectorResource(id = R.drawable.ic_baseline_mic) else ImageVector.vectorResource(id = R.drawable.ic_baseline_mic_off)
        val pauseIcon = if(state.callsQueue.size==1) ImageVector.vectorResource(id = R.drawable.ic_baseline_pause) else ImageVector.vectorResource(id = R.drawable.ic_baseline_swap_calls)
        val pauseText = if(state.callsQueue.size==1) "Retener" else "Intercambiar"

        AnimatedVisibility(
            visible = !state.incomingCall && state.showDialPad,
            enter = scaleIn(animationSpec = tween(durationMillis = 200, delayMillis = 200)),
            exit = scaleOut(animationSpec = tween(durationMillis = 200))
        ) {
            CallDialPad(viewModel = viewModel)
        }

        AnimatedVisibility(
            visible = !state.incomingCall && !state.showDialPad,
            enter = scaleIn(animationSpec = tween(durationMillis = 200)),
            exit = scaleOut(animationSpec = tween(durationMillis = 200))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround,
                modifier = Modifier.fillMaxWidth()
            ) {

                Column(
                    verticalArrangement = Arrangement.spacedBy(1.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = pauseIcon,
                        contentDescription = null,
                        modifier = (if (!state.paused) inactiveBtnModifier else activeBtnModifier)
                            .clickable {
                                if (!state.paused)
                                    viewModel.setOnHold()
                                else
                                    viewModel.setUnHold()
                            }
                            .padding(12.dp),
                        tint = Color.White
                    )
                    Text(
                        text = pauseText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Light,
                        color = Color.White
                    )
                }

            }
        }
        AnimatedVisibility(
            visible = !state.incomingCall,
            enter = scaleIn(animationSpec = tween(durationMillis = 200)),
            exit = scaleOut(animationSpec = tween(durationMillis = 200))
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround,
                modifier = Modifier.fillMaxWidth()
            ) {

                Column(
                    verticalArrangement = Arrangement.spacedBy(1.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = micIcon,
                        contentDescription = null,
                        modifier = (if (!state.micOff) inactiveBtnModifier else activeBtnModifier)
                            .clickable {
                                if (!state.micOff)
                                    viewModel.setMicOff()
                                else
                                    viewModel.setMicOn()
                            }
                            .padding(12.dp),
                        tint = Color.White
                    )
                    Text(
                        text = "Silenciar",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Light,
                        color = Color.White
                    )
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(1.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_baseline_dialpad),
                        contentDescription = null,
                        modifier = (if (!state.showDialPad) inactiveBtnModifier else activeBtnModifier)
                            .clickable {
                                viewModel.showDialPad()
                            }
                            .padding(12.dp),
                        tint = Color.White
                    )
                    Text(
                        text = "Teclado",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Light,
                        color = Color.White
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(1.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_baseline_speaker),
                        contentDescription = null,
                        modifier = (if (!state.speakerOn) inactiveBtnModifier else activeBtnModifier)
                            .clickable {
                                if (!state.speakerOn)
                                    viewModel.setSpeakerOn()
                                else
                                    viewModel.setSpeakerOff()
                            }
                            .padding(12.dp),
                        tint = Color.White
                    )
                    Text(
                        text = "Altavoz",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Light,
                        color = Color.White
                    )
                }
            }
        }

        Row (
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround,
            modifier = Modifier.fillMaxWidth().animateContentSize()
        ){
            AnimatedVisibility(
                visible = state.incomingCall,
                enter = scaleIn(animationSpec = tween(durationMillis = 200)),
                exit = scaleOut(animationSpec = tween(durationMillis = 200)),
                modifier = Modifier.padding(12.dp)
            ){
                FloatingActionButton(onClick = {
                    MyCallsManager.answerRingingCall()
                },
                    containerColor = green_700,
                    contentColor = Color.White,
                    modifier = Modifier.scale(scale)
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_call_24),
                        contentDescription = null
                    )
                }
            }
            AnimatedVisibility(
                visible = state.callsQueue.isNotEmpty() && !state.showDialPad,
                enter = scaleIn(animationSpec = tween(durationMillis = 200)),
                exit = scaleOut(animationSpec = tween(durationMillis = 200)),
                modifier = Modifier.padding(12.dp)
            ) {
                FloatingActionButton(
                    onClick = {
                        MyCallsManager.disconnectCall()
                        val mgr = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        mgr.cancel(MyCallsManager.inCallNotificationId)
                    },
                    containerColor = red_700,
                    contentColor = Color.White
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_call_end_24),
                        contentDescription = null
                    )
                }
            }

        }

        if(MyCallsManager.thereIsIncomingCall()){
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}