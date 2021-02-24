package com.edxavier.cerberus_sms

import android.content.ContextWrapper
import androidx.multidex.MultiDexApplication
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.firebase.FirebaseApp
import com.pixplicity.easyprefs.library.Prefs

class BaseApp: MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)

        MobileAds.initialize(this)
        val requestConfig = RequestConfiguration.Builder()
                .setTestDeviceIds(arrayOf(
                        "AC5F34885B0FE7EF03A409EB12A0F949",
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