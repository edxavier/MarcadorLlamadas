package com.edxavier.cerberus_sms.ui.screens.incall

import android.app.NotificationManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
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
                InCallActionButton(
                    icon = { Icon(imageVector = pauseIcon, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp)) },
                    label = pauseText,
                    isActive = state.paused,
                    onClick = {
                        if (!state.paused) viewModel.setOnHold() else viewModel.setUnHold()
                    },
                    activeTint = Color(0xFFFFC107)
                )
                InCallActionButton(
                    icon = { Icon(imageVector = micIcon, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp)) },
                    label = "Silenciar",
                    isActive = state.micOff,
                    onClick = {
                        if (!state.micOff) viewModel.setMicOff() else viewModel.setMicOn()
                    },
                    activeTint = Color(0xFFEF4444)
                )
                InCallActionButton(
                    icon = { Icon(imageVector = ImageVector.vectorResource(id = R.drawable.ic_baseline_dialpad), contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp)) },
                    label = "Teclado",
                    isActive = state.showDialPad,
                    onClick = { viewModel.showDialPad() },
                    activeTint = Color(0xFF3B82F6)
                )
                InCallActionButton(
                    icon = { Icon(imageVector = ImageVector.vectorResource(id = R.drawable.ic_baseline_speaker), contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp)) },
                    label = "Altavoz",
                    isActive = state.speakerOn,
                    onClick = {
                        if (!state.speakerOn) viewModel.setSpeakerOn() else viewModel.setSpeakerOff()
                    },
                    activeTint = Color(0xFF3B82F6)
                )
            }
        }

        Row(
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

@Composable
private fun InCallActionButton(
    icon: @Composable () -> Unit,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    activeTint: Color = Color.White
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (isActive) activeTint.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.08f),
        tonalElevation = if (isActive) 4.dp else 1.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            icon()
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Light,
                color = Color.White,
                letterSpacing = 0.2.sp
            )
        }
    }
}
