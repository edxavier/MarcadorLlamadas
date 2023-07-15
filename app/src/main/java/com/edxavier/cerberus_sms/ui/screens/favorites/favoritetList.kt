package com.edxavier.cerberus_sms.ui.screens.favorites

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.edxavier.cerberus_sms.data.models.Contact

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FavoritesList(
    contactList: List<Contact>,
    onContactClick: (Contact)-> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(8.dp)
    ){
       items(contactList){
           FavoriteEntry(contact = it, onClick = { onContactClick(it) })
       }
    }
}