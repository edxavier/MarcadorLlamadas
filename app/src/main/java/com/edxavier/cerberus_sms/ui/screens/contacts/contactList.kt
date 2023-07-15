package com.edxavier.cerberus_sms.ui.screens.contacts

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edxavier.cerberus_sms.data.models.Contact
import com.edxavier.cerberus_sms.ui.screens.calls.CallLogEntry

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ContactList(
    contactList: List<Contact>,
    onContactClick: (Contact)-> Unit
) {
    val group = contactList.groupBy{ it.name.substring(0,1)}
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 2.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ){
        group.forEach { (key, values) ->
            stickyHeader {
                Text(
                    text = key,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface)
                        .clip(RoundedCornerShape(2.dp))
                        .padding(4.dp).fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp
                )
            }
            items(items = values){
                ContactEntry(contact = it, onClick = { onContactClick(it) })
            }
        }

    }
}