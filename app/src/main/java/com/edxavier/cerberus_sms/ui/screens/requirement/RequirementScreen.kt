package com.edxavier.cerberus_sms.ui.screens.requirement

import android.app.role.RoleManager
import android.content.Intent
import android.telecom.TelecomManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.edxavier.cerberus_sms.R
import com.edxavier.cerberus_sms.helpers.hasRequiredPermissions
import com.edxavier.cerberus_sms.navigation.Routes
import com.edxavier.cerberus_sms.ui.ui.theme.white_50

@Composable
fun RequirementScreen(navController: NavController) {
    val state = remember {
        MutableTransitionState(false).apply {
            targetState = true
        }
    }
    val myContext = LocalContext.current

    val requestSetDefaultApp = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = {
            if (it.resultCode == android.app.Activity.RESULT_OK) {
                val nextRoute = if (myContext.hasRequiredPermissions()) Routes.Calls.route else Routes.Permissions.route
                navController.navigate(nextRoute){
                    val route = navController.currentBackStackEntry?.destination?.route
                    route?.apply {
                        popUpTo(route) {
                            inclusive =  true
                        }
                    }
                    launchSingleTop = true
                }
            }
        }
    )

    val title = buildAnnotatedString {
        append("Configurar ")
        withStyle(
            style = SpanStyle(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.tertiary
            )
        ) {append(LocalContext.current.getString(R.string.app_name)) }
        append(" como predeterminado")
    }
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Image(
                painter = painterResource(id = R.drawable.default_app),
                contentDescription = null,
                modifier = Modifier.size(220.dp)
                    .background(shape = RoundedCornerShape(12.dp), color = white_50)
                    .padding(horizontal = 20.dp)
            )

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp),
                text = title,
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                text = "${LocalContext.current.getString(R.string.app_name)} solo puede iniciar y recibir llamadas si es su aplicación de teléfono predeterminada",
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Light,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    val roleManager = myContext.getSystemService(AppCompatActivity.ROLE_SERVICE) as RoleManager
                    val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER)
                    requestSetDefaultApp.launch(intent)
                }else if (myContext.getSystemService(TelecomManager::class.java).defaultDialerPackage !== myContext.packageName) {
                    val changeDialer = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER)
                    changeDialer.putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, myContext.packageName)
                    requestSetDefaultApp.launch(changeDialer)
                }
            }) {
                Text(text = "Establecer por defecto")
            }
        }
    }
}