package com.edxavier.cerberus_sms.ui.screens.contacts

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.edxavier.cerberus_sms.R
import com.edxavier.cerberus_sms.helpers.hasRequiredPermissions
import com.edxavier.cerberus_sms.navigation.Routes
import com.edxavier.cerberus_sms.ui.calls.AppViewModel
import com.edxavier.cerberus_sms.ui.core.ui.LoadingIndicator
import com.edxavier.cerberus_sms.ui.screens.permissions.PermissionRequiredScreen
import com.edxavier.cerberus_sms.ui.screens.calls.comp.NoDataScreen
import com.edxavier.cerberus_sms.ui.core.states.UiState

@Composable
fun ContactsResults(
    state: UiState,
    viewModel: AppViewModel,
    navController: NavHostController
) {

    if(state.isLoading && state.contacts.isEmpty()){
        LoadingIndicator()
    }else if(!state.isLoading && state.contacts.isEmpty()){
        NoDataScreen(message = "No hay contactos para mostrar", imageId = R.drawable.contacts_book)
    }else{
        ContactList(
            contactList = state.contacts,
            onContactClick = {
                viewModel.selectedContact = it
                navController.navigate(Routes.ContactDetail.route)
            }
        )
    }
}