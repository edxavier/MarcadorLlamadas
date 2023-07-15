package com.edxavier.cerberus_sms.navigation

sealed class Routes (
    val route: String,
){
    object Calls: Routes("calls")
    object Contacts: Routes("contacts")
    object Favorites: Routes("favorites")
    object Settings: Routes("settings")
    object Required: Routes("required")
    object Permissions: Routes("permissions")
    object CallHistory: Routes("call_history")
    object ContactDetail: Routes("contact_detail")
}