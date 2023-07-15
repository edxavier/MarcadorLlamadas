package com.edxavier.cerberus_sms.ui.screens.incall

import android.telecom.Call
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.edxavier.cerberus_sms.R
import com.edxavier.cerberus_sms.data.repositories.RepoOperator
import com.edxavier.cerberus_sms.helpers.getOperatorString
import com.edxavier.cerberus_sms.helpers.getPhoneNumber
import com.edxavier.cerberus_sms.helpers.stateToString
import com.edxavier.cerberus_sms.helpers.timeFormat
import kotlinx.coroutines.delay
import java.util.*

@Composable
fun MultiOngoingCall(
    call: InCallDto
) {
    val ctx = LocalContext.current
    var operator by remember { mutableStateOf("") }
    var counter by remember { mutableStateOf(0) }
    var country by remember { mutableStateOf("") }
    val eSeconds = ((Date().time - call.call.details.connectTimeMillis)/1000).toInt()
    var timer by remember { mutableStateOf(eSeconds) }

    LaunchedEffect(key1 = counter) {
        delay(1000L)
        val connectTime = call.call.details.connectTimeMillis
        timer = if(connectTime<=0L){
            -1
        }else{
            ((Date().time - call.call.details.connectTimeMillis)/1000).toInt()
        }
        counter++
    }

    LaunchedEffect(key1 = true){
        val repOpe = RepoOperator(ctx)
        val ope = repOpe.getOperator(call.call.getPhoneNumber())
        ope?.let {
            operator = it.operator.getOperatorString()
            country = "${it.area} ${it.country}"
        }
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ){
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = call.contact.name,
            fontSize = 20.sp,
            fontWeight = FontWeight.Light,
            color = Color.White, modifier = Modifier.weight(1f))
        if(call.call.state!=Call.STATE_ACTIVE) {
            Text(
                text = call.call.state.stateToString(),
                fontSize = 14.sp, fontWeight = FontWeight.Light, color = Color.White
            )
        }else{
            Text(
                text = timer.timeFormat(),
                fontSize = 14.sp, fontWeight = FontWeight.Light, color = Color.White
            )
        }
    }
}
