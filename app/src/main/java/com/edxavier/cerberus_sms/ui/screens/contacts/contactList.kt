package com.edxavier.cerberus_sms.ui.screens.contacts

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.edxavier.cerberus_sms.data.models.Contact

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ContactList(
    contactList: List<Contact>,
    onContactClick: (Contact) -> Unit
) {
    val group = contactList.groupBy { it.name.substring(0, 1).uppercase() }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        group.forEach { (key, values) ->
            stickyHeader {
                Surface(
                    tonalElevation = 1.dp,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                ) {
                    Text(
                        text = key,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
            items(items = values) {
                ContactEntry(contact = it, onClick = { onContactClick(it) })
            }
        }
    }
}
