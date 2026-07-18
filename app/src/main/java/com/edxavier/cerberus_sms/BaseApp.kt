package com.edxavier.cerberus_sms

import android.content.ContextWrapper
import androidx.multidex.MultiDexApplication
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.firebase.FirebaseApp
import com.edxavier.cerberus_sms.helpers.AnalyticsLogger
import com.pixplicity.easyprefs.library.Prefs

class BaseApp: MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)

        AnalyticsLogger.init(this)
        MobileAds.initialize(this)
        val requestConfig = RequestConfiguration.Builder()
                .setTestDeviceIds(arrayOf(
                        "2391D45C5FBB101C3B5A692B3E866DFB",
                        AdRequest.DEVICE_ID_EMULATOR
                ).toList())
                .build()
        MobileAds.setRequestConfiguration(requestConfig)

        Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(packageName)
                .setUseDefaultSharedPreference(true)
                .build()
    }
}