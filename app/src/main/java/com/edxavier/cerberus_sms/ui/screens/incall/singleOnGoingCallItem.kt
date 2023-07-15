package com.edxavier.cerberus_sms.ui.screens.incall

import android.telecom.Call
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.edxavier.cerberus_sms.helpers.*
import kotlinx.coroutines.delay
import java.util.*

@Composable
fun SingleOngoingCall(
    call: InCallDto
) {
    val ctx = LocalContext.current
    var timer by remember { mutableStateOf(call.elapsedSeconds) }
    var counter by remember { mutableStateOf(0) }
    var operator by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var opColor by remember { mutableStateOf(0) }

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
            opColor = it.operator.getOperatorColor(ctx)
            country = if(it.area.isNotEmpty()) "${it.area}, ${it.country}" else it.country
        }
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(1.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Spacer(modifier = Modifier.height(6.dp))
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(call.contact.photo)
                .crossfade(true)
                .build(),
            contentDescription = null,
            placeholder = painterResource(id = R.drawable.ic_user),
            error = painterResource(id = R.drawable.ic_user),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(78.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = call.contact.name,
            fontSize = 24.sp, fontWeight = FontWeight.Light, color = Color.White)
        if(call.contact.name!=call.contact.number) {
            Text(
                text = call.call.getPhoneNumber().toPhoneFormat(),
                fontSize = 16.sp, fontWeight = FontWeight.Light, color = Color.White
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if(!operator.startsWith("INTER") && !operator.startsWith("LINEA")) {
                Text(
                    text = operator.uppercase(),
                    fontSize = 12.sp, fontWeight = FontWeight.Light, color = Color.White,
                    modifier = Modifier
                        .background(
                            Color(opColor),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 2.dp),
                    lineHeight = 14.sp
                )
            }
            if(country!="Nicaragua") {
                Text(
                    text = country,
                    fontSize = 12.sp, fontWeight = FontWeight.Light, color = Color.White
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        if(call.call.state!=Call.STATE_ACTIVE) {
            Text(
                text = call.call.state.stateToString(),
                fontSize = 12.sp, fontWeight = FontWeight.Normal, color = Color.White
            )
        }else{
            Text(
                text = timer.timeFormat(),
                fontSize = 12.sp, fontWeight = FontWeight.Normal, color = Color.White
            )
        }
    }
}