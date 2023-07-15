package com.edxavier.cerberus_sms.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.edxavier.cerberus_sms.R
import com.edxavier.cerberus_sms.ui.calls.AppViewModel

@Composable
fun BottomNavBar(
    navController: NavHostController,
    viewModel: AppViewModel
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val state by viewModel.uiState.collectAsState()
    val navItems = listOf(
        NavItem(
            name = "Llamadas",
            route = Routes.Calls.route,
            icon = ImageVector.vectorResource(id = R.drawable.ic_call_24)
        ),
        NavItem(
            name = "Contactos",
            route = Routes.Contacts.route,
            icon = ImageVector.vectorResource(id = R.drawable.ic_contacts),
        ),
        NavItem(
            name = "Favoritos",
            route = Routes.Favorites.route,
            icon = Icons.Rounded.Star,
        ),
        NavItem(
            name = "Ajustes",
            route = Routes.Settings.route,
            icon = Icons.Rounded.Settings,
        )
    )
    var showBottomBar by remember {
        mutableStateOf(true)
    }
    // Show bottom bar if destination route is one of the bottomBar routes
    showBottomBar = navItems.any { it.route ==  backStackEntry?.destination?.route}
    AnimatedVisibility(
        visible = showBottomBar && !state.dialShown,
        enter = slideInVertically(animationSpec = tween(durationMillis = 300)) { fullHeight ->
            fullHeight
        },
        exit = slideOutVertically(animationSpec = tween(durationMillis = 300)) { fullHeight ->
            fullHeight
        }
    ) {
        NavigationBar(
        ){
            navItems.forEach { item ->
                val selected = item.route == backStackEntry?.destination?.route
                NavigationBarItem(
                    selected = selected,
                    onClick = {
                        // Avoid nav history on navbar
                        navController.navigate(item.route) {
                            val route = navController.currentBackStackEntry?.destination?.route
                            route?.apply {
                                popUpTo(route) {
                                    inclusive = true
                                }
                            }
                            launchSingleTop = true
                        }
                    },
                    label = {
                        Text(
                            text = item.name, fontSize = 10.sp, fontWeight = FontWeight.Light
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = "${item.name} Icon",
                            modifier = Modifier.size(20.dp)
                        )
                    },

                    )
            }
        }
    }
}