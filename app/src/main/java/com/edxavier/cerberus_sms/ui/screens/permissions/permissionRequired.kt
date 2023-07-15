package com.edxavier.cerberus_sms.ui.screens.permissions

import android.Manifest
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.edxavier.cerberus_sms.R
import com.edxavier.cerberus_sms.helpers.hasRequiredPermissions
import com.edxavier.cerberus_sms.navigation.Routes
import com.edxavier.cerberus_sms.ui.ui.theme.white_50

@Composable
fun PermissionRequiredScreen(
    navController: NavController
) {
    val myContext = LocalContext.current
    val requestPerms = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(), onResult = {
            if(myContext.hasRequiredPermissions()) {
                navController.navigate(Routes.Calls.route) {
                    val route = navController.currentBackStackEntry?.destination?.route
                    route?.apply {
                        popUpTo(route) {
                            inclusive = true
                        }
                    }
                    launchSingleTop = true
                }
            }else{
                Toast.makeText(myContext, "Permisos requeridos no asignados", Toast.LENGTH_LONG).show()
            }
        } )
    Box(
        modifier = Modifier.fillMaxSize().padding(12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.secure_file_icon),
                contentDescription = null,
                modifier = Modifier.size(84.dp)
            )
            val title = buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                ) {append(LocalContext.current.getString(R.string.app_name)) }
                append(" requiere permisos para: ")
                withStyle(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.error
                    )
                ) {append("Leer tus contactos, ") }
                withStyle(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.error
                    )
                ) {append("Leer y gestionar las llamadas del teléfono") }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = title, fontWeight = FontWeight.Light, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = {
                val perms = ArrayList<String>()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    perms.add(Manifest.permission.READ_PHONE_NUMBERS)
                }
                perms.add(Manifest.permission.READ_PHONE_STATE)
                perms.add(Manifest.permission.READ_CALL_LOG)
                perms.add(Manifest.permission.READ_CONTACTS)
                perms.add(Manifest.permission.CALL_PHONE)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    perms.add(Manifest.permission.POST_NOTIFICATIONS)
                }
                requestPerms.launch(perms.toTypedArray())
            }) {
                Text(text = "Conceder permisos")
            }

        }
    }
}