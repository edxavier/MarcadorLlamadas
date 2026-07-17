package com.edxavier.cerberus_sms.ui.screens.incall

import android.telecom.Call
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.style.TextAlign
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
fun MultiOngoingCall(call: InCallDto) {
    val ctx = LocalContext.current
    var counter by remember { mutableStateOf(0) }
    val eSeconds = ((Date().time - call.call.details.connectTimeMillis) / 1000).toInt()
    var timer by remember { mutableStateOf(eSeconds) }

    LaunchedEffect(key1 = counter) {
        delay(1000L)
        val connectTime = call.call.details.connectTimeMillis
        timer = if (connectTime <= 0L) -1
        else ((Date().time - call.call.details.connectTimeMillis) / 1000).toInt()
        counter++
    }

    Surface(
        tonalElevation = 2.dp,
        shape = RoundedCornerShape(16.dp),
        color = Color.Black.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
                    .size(40.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = call.contact.name,
                color = Color.White,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            if (call.call.state != Call.STATE_ACTIVE) {
                Text(
                    text = call.call.state.stateToString(),
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            } else {
                Text(
                    text = timer.timeFormat(),
                    fontSize = 14.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}
