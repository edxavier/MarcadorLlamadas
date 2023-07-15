package com.edxavier.cerberus_sms.ui.screens.favorites

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
fun FavoritesResults(
    state: UiState,
    viewModel: AppViewModel,
    navController: NavHostController
) {
    val myContext = LocalContext.current

    if(state.isLoading && state.contacts.isEmpty()){
        LoadingIndicator()
    }else if(!state.isLoading && state.contacts.isEmpty()){
        NoDataScreen(message = "Nadie esta en tu lista de favoritos aun", imageId = R.drawable.folder_favorite_icon)
    }else{
        FavoritesList(
            contactList = state.contacts,
            onContactClick = {
                viewModel.selectedContact = it
                navController.navigate(Routes.ContactDetail.route)
            }
        )
    }
}