package com.edxavier.cerberus_sms.ui.screens.favorites

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.edxavier.cerberus_sms.R
import com.edxavier.cerberus_sms.navigation.Routes
import com.edxavier.cerberus_sms.ui.calls.AppViewModel
import com.edxavier.cerberus_sms.ui.core.ui.LoadingIndicator
import com.edxavier.cerberus_sms.ui.screens.calls.comp.NoDataScreen
import com.edxavier.cerberus_sms.ui.screens.contacts.ContactsResults
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(viewModel: AppViewModel, navController: NavHostController) {
    val state by viewModel.uiState.collectAsState()
    var searchText by remember{ mutableStateOf("") }
    var active by remember{ mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(true){
        viewModel.getContacts(justFavorites = true)
    }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    Scaffold(
        topBar = {
            SearchBar(
                query = searchText,
                onQueryChange = {
                    searchText = it
                    scope.launch {
                        viewModel.getContacts(it, justFavorites = true)
                    }
                },
                onSearch = {
                    active = false
                    scope.launch {
                        viewModel.getContacts(it, justFavorites = true)
                    }
                },
                active = active,
                onActiveChange = {
                    active = it
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp),
                shape = RoundedCornerShape(12.dp),
                placeholder = { Text(text = "Buscar contacto")},
                leadingIcon = {
                    val icon = if(!active) Icons.Default.Search else Icons.Default.ArrowBack
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.clickable {
                            active = false
                            searchText = ""
                            scope.launch {
                                viewModel.getContacts(justFavorites = true)
                            }
                        }
                    )},
                trailingIcon = {
                    if(searchText.isNotEmpty()){
                        Icon(
                            imageVector = Icons.Rounded.Close, contentDescription = null,
                            modifier = Modifier.clickable {
                                searchText = ""
                                scope.launch {
                                    viewModel.getContacts(justFavorites = true)
                                }
                            }
                        )
                    }
                },
            ){
                ContactsResults(
                    state = state,
                    viewModel = viewModel,
                    navController = navController
                )
            }
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) {
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(it)){
            FavoritesResults(
                state = state,
                viewModel = viewModel,
                navController = navController
            )
        }
    }

}

