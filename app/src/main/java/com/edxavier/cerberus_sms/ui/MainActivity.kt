package com.edxavier.cerberus_sms.ui

import android.os.Bundle
import android.telephony.PhoneNumberUtils
import android.util.DisplayMetrics
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.edxavier.cerberus_sms.R
import com.edxavier.cerberus_sms.data.models.Operator
import com.edxavier.cerberus_sms.data.repositories.RepoContact
import com.edxavier.cerberus_sms.helpers.*
import com.edxavier.cerberus_sms.navigation.BottomNavBar
import com.edxavier.cerberus_sms.navigation.Routes
import com.edxavier.cerberus_sms.ui.calls.AppViewModel
import com.edxavier.cerberus_sms.ui.screens.calls.CallHistory
import com.edxavier.cerberus_sms.ui.screens.calls.CallLogScreen
import com.edxavier.cerberus_sms.ui.screens.permissions.PermissionRequiredScreen
import com.edxavier.cerberus_sms.ui.screens.contacts.ContactDetailsScreen
import com.edxavier.cerberus_sms.ui.screens.contacts.ContactsScreen
import com.edxavier.cerberus_sms.ui.screens.favorites.FavoritesScreen
import com.edxavier.cerberus_sms.ui.screens.requirement.RequirementScreen
import com.edxavier.cerberus_sms.ui.screens.settings.SettingsScreen
import com.edxavier.cerberus_sms.ui.ui.theme.AppTheme
import com.google.android.gms.ads.AdSize
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    lateinit var viewModel: AppViewModel

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            FlowEventBus.subscribe<String> {
                lifecycleScope.launch {
                    viewModel.getCallLog()
                }
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_MarcadorLlamadasM3)
        MyCallsManager.adSize = getAdSize()
        val defaultDialer = isDefaultDialerApp()
        val hasPermission = hasRequiredPermissions()

        viewModel = AppViewModel(RepoContact(this))
        handleCallIntent()
        val defaultRoute = if(defaultDialer){
            if(hasPermission){
                Routes.Calls.route
            }else{
                Routes.Permissions.route
            }
        }else{
            Routes.Required.route
        }
        lifecycleScope.launch {
            Operator.initializeOperators(this@MainActivity)
        }

        setContent {
            val navController = rememberNavController()

            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold(
                        bottomBar = { BottomNavBar(navController = navController, viewModel) }
                    ) { paddingValues->
                        NavHost(
                            navController = navController,
                            startDestination = defaultRoute,
                            modifier = Modifier.padding(paddingValues)
                        ) {
                            composable(Routes.Calls.route){
                                CallLogScreen(viewModel = viewModel, navCtrl = navController)
                            }
                            composable(Routes.Contacts.route){
                                ContactsScreen(viewModel = viewModel, navController = navController)
                            }
                            composable(Routes.Favorites.route){
                                FavoritesScreen(viewModel = viewModel, navController = navController)
                            }
                            composable(Routes.Settings.route){
                                SettingsScreen(viewModel=viewModel, navController = navController)
                            }
                            composable(Routes.Required.route){
                                RequirementScreen(navController =navController)
                            }
                            composable(Routes.Permissions.route){
                                PermissionRequiredScreen(navController=navController)
                            }
                            composable(Routes.CallHistory.route){
                                CallHistory(viewModel=viewModel, navController = navController)
                            }
                            composable(Routes.ContactDetail.route){
                                ContactDetailsScreen(viewModel=viewModel, navController = navController)
                            }
                        }
                    }
                }
            }
        }
    }
    private fun getAdSize(): AdSize {
        //Determine the screen width to use for the ad width.
        val display = windowManager.defaultDisplay
        val outMetrics = DisplayMetrics()
        display.getMetrics(outMetrics)
        val widthPixels = outMetrics.widthPixels.toFloat()
        val density = outMetrics.density

        //you can also pass your selected width here in dp
        val adWidth = (widthPixels / density).toInt()

        //return the optimal size depends on your orientation (landscape or portrait)
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth)
    }
    private fun handleCallIntent() {
        if (!intent.dataString.isNullOrEmpty()){
            val callPhone = PhoneNumberUtils.getNumberFromIntent(intent, this).clearPhoneString()
            viewModel.setCallNumberIntent(callPhone)
        }
    }
}
