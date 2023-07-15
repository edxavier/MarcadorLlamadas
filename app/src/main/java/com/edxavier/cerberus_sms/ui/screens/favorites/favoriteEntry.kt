package com.edxavier.cerberus_sms.ui.screens.favorites

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.edxavier.cerberus_sms.R
import com.edxavier.cerberus_sms.data.models.Contact

@Composable
fun FavoriteEntry(
    contact: Contact,
    onClick: ()-> Unit
) {

    Column(
        modifier = Modifier
            .clickable { onClick() }
            .fillMaxWidth().padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
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
            modifier = Modifier.size(72.dp).clip(CircleShape)
        )
        Text(
            text = contact.name, style = MaterialTheme.typography.titleMedium,
            softWrap = false, overflow = TextOverflow.Ellipsis,
        )
    }
}