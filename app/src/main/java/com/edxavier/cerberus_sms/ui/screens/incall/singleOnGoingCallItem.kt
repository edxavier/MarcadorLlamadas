package com.edxavier.cerberus_sms.ui.screens.incall

import android.telecom.Call
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
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
fun SingleOngoingCall(call: InCallDto) {
    val ctx = LocalContext.current
    var timer by remember { mutableStateOf(call.elapsedSeconds) }
    var counter by remember { mutableStateOf(0) }
    var operator by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var opColor by remember { mutableStateOf(0) }

    LaunchedEffect(key1 = counter) {
        delay(1000L)
        val connectTime = call.call.details.connectTimeMillis
        timer = if (connectTime <= 0L) -1
        else ((Date().time - call.call.details.connectTimeMillis) / 1000).toInt()
        counter++
    }

    LaunchedEffect(key1 = true) {
        val repOpe = RepoOperator(ctx)
        val ope = repOpe.getOperator(call.call.getPhoneNumber())
        ope?.let {
            operator = it.operator.getOperatorString()
            opColor = it.operator.getOperatorColor(ctx)
            country = if (it.area.isNotEmpty()) "${it.area}, ${it.country}" else it.country
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Glassmorphism card container
        Surface(
            tonalElevation = 3.dp,
            shape = RoundedCornerShape(28.dp),
            color = Color.Black.copy(alpha = 0.15f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Photo with glow ring
                Box(
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .size(96.dp)
                        .clip(CircleShape)
                        .border(2.5.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color.White.copy(alpha = 0.15f), Color.Transparent)
                            )
                        )
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
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Contact name
                Text(
                    text = call.contact.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                if (call.contact.name != call.contact.number) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = call.call.getPhoneNumber().toPhoneFormat(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Operator + Country chips
                val showOperator = operator.isNotBlank() && !operator.startsWith("INTER") && !operator.startsWith("LINEA")
                val showCountry = country.isNotBlank() && country != "Nicaragua"
                if (showOperator || showCountry) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (showOperator) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(opColor).copy(alpha = 0.9f)
                            ) {
                                Text(
                                    text = operator.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            if (showCountry) {
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                        }
                        if (showCountry) {
                            Icon(
                                imageVector = Icons.Default.Public,
                                contentDescription = "País",
                                modifier = Modifier.size(14.dp),
                                tint = Color.White.copy(alpha = 0.6f)
                            )
                            if (showOperator) {
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = country,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Call state or timer
                if (call.call.state != Call.STATE_ACTIVE) {
                    Text(
                        text = call.call.state.stateToString(),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                } else {
                    Text(
                        text = timer.timeFormat(),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "duración",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.4f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
