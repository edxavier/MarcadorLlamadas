package com.edxavier.cerberus_sms

import android.Manifest
import android.app.role.RoleManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.telecom.TelecomManager
import android.util.DisplayMetrics
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.edxavier.cerberus_sms.data.models.Operator
import com.edxavier.cerberus_sms.data.repositories.RepoContact
import com.edxavier.cerberus_sms.databinding.ActivityMainBinding
import com.google.android.gms.ads.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.nicrosoft.consumoelectrico.ScopeActivity
import com.pixplicity.easyprefs.library.Prefs
import kotlinx.coroutines.launch


class MainActivity : ScopeActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(setOf(
                R.id.nav_calls, R.id.nav_contacts, R.id.nav_preferences))
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.bottomNavView.setupWithNavController(navController)
        launch {
            Operator.initializeOperators(this@MainActivity)
        }
        setupBanner()
        val requested = Prefs.getBoolean("start_requested_role", false)
        if(!requested) {
            requestRole()
            Prefs.putBoolean("start_requested_role", true)
        }

    }
    private val REQUEST_ID = 1

    private fun requestRole() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(ROLE_SERVICE) as RoleManager
            val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER)
            startActivityForResult(intent, REQUEST_ID)
        }else if (getSystemService(TelecomManager::class.java).defaultDialerPackage !== packageName) {
            val changeDialer = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER)
            changeDialer.putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, packageName)
            startActivity(changeDialer)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ID) {
            if (resultCode == android.app.Activity.RESULT_OK) {
                // Your app is now the default dialer app
            } else {
                // Your app is not the default dialer app
            }
        }
    }
    private fun setupBanner() {

        val adRequest = AdRequest.Builder()
                .build()

        val adView =  AdView(this)
        binding.adViewContainer.addView(adView)

        adView.setAdSize(getAdSize())
        adView.adUnitId = getString(R.string.BANNER_FLOTANTE)

        adView.loadAd(adRequest)
        //nav_view.menu.findItem(R.id.destino_ocultar_publicidad).isVisible = false
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

}