package com.edxavier.cerberus_sms.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Star
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.edxavier.cerberus_sms.R


data class NavItem(
    val name: String,
    val route: String,
    val icon: ImageVector,
)