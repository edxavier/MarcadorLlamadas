package com.edxavier.cerberus_sms.ui.screens.contacts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.edxavier.cerberus_sms.R
import com.edxavier.cerberus_sms.data.models.Contact

@Composable
fun ContactEntry(
    contact: Contact,
    onClick: ()-> Unit
) {
    Row(
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ){
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(contact.photo)
                .crossfade(true)
                .build(),
            contentDescription = null,
            placeholder = painterResource(id = R.drawable.ic_user),
            error = painterResource(id = R.drawable.ic_user),
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(42.dp).clip(CircleShape)
        )
        Text(
            text = contact.name,
            softWrap = false, overflow = TextOverflow.Ellipsis,
            fontSize = 14.sp
        )

    }
}