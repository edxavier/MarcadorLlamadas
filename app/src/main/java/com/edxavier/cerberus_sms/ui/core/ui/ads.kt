package com.edxavier.cerberus_sms.ui.core.ui

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.util.Log
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.load
import coil.transform.RoundedCornersTransformation
import com.edxavier.cerberus_sms.R
import com.edxavier.cerberus_sms.databinding.AdNativeInCallBinding
import com.edxavier.cerberus_sms.helpers.MyCallsManager
import com.edxavier.cerberus_sms.helpers.visible
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import kotlinx.coroutines.delay

@Composable
fun MyBannerAd(modifier: Modifier = Modifier) {
    var refreshTrigger by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000)
            refreshTrigger++
        }
    }

    key(refreshTrigger) {
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { context ->
                var retryCount = 0
                val handler = Handler(Looper.getMainLooper())
                AdView(context).apply {
                    setAdSize(MyCallsManager.adSize)
                    adUnitId = context.getString(R.string.BANNER_PRINCIPAL)
                    adListener = object : AdListener() {
                        override fun onAdFailedToLoad(error: LoadAdError) {
                            Log.w("Ads", "Banner failed (retry $retryCount): ${error.message}")
                            if (retryCount < 3) {
                                retryCount++
                                handler.postDelayed({
                                    loadAd(AdRequest.Builder().build())
                                }, (retryCount * 5000).toLong())
                            }
                        }
                    }
                    loadAd(AdRequest.Builder().build())
                }
            }
        )
    }
}

@Composable
fun SmartAd() {
    var useNative by remember { mutableStateOf(true) }
    var refreshTrigger by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000)
            refreshTrigger++
            useNative = true
        }
    }

    key(refreshTrigger) {
        Crossfade(targetState = useNative, label = "ad") { isNative ->
            if (isNative) {
                NativeMediumAd(onGiveUp = { useNative = false })
            } else {
                MyBannerAd()
            }
        }
    }
}

@Composable
fun NativeMediumAd(onGiveUp: () -> Unit = {}) {
    var refreshTrigger by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000)
            refreshTrigger++
        }
    }

    key(refreshTrigger) {
        AndroidView(factory = { context ->
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val adNativeInCall = AdNativeInCallBinding.inflate(inflater)
            val adView = adNativeInCall.root.also { adView ->
                adView.advertiserView = adNativeInCall.adAdvertiser
                adView.bodyView = adNativeInCall.adBodyText
                adView.callToActionView = adNativeInCall.adBtnCallToAction
                adView.headlineView = adNativeInCall.adHeadline
                adView.iconView = adNativeInCall.adIcon
                adView.starRatingView = adNativeInCall.adStarRating
            }

            var retryCount = 0
            val handler = Handler(Looper.getMainLooper())

            val builder = AdLoader.Builder(context, context.getString(R.string.NATIVE_AD))
                .withAdListener(object : AdListener() {
                    override fun onAdLoaded() {
                        Log.d("Ads", "Native ad loaded")
                        adView.visible()
                        adNativeInCall.NativeLinearLayout.visible()
                    }
                    override fun onAdFailedToLoad(error: LoadAdError) {
                        Log.w("Ads", "Native ad failed: ${error.message} → falling back to banner")
                        onGiveUp()
                    }
                })

            builder.forNativeAd { nativeAd ->
                populateNativeAd(nativeAd, adNativeInCall)
                adView.setNativeAd(nativeAd)
                adView.setAdChoicesView(adNativeInCall.adChoices)
            }
            val adLoader = builder.build()
            adLoader.loadAd(AdRequest.Builder().build())
            return@AndroidView adView
        })
    }
}

private fun populateNativeAd(nativeAd: com.google.android.gms.ads.nativead.NativeAd, binding: AdNativeInCallBinding) {
    binding.adHeadline.text = nativeAd.headline
    nativeAd.advertiser?.let {
        binding.adAdvertiser.text = it
        binding.adAdvertiser.visible()
    }
    nativeAd.icon?.let {
        binding.adIcon.load(it.drawable) { transformations(RoundedCornersTransformation(radius = 8f)) }
        binding.adIcon.visible()
    }
    nativeAd.starRating?.let {
        binding.adStarRating.rating = it.toFloat()
        binding.adStarRating.visible()
    }
    nativeAd.callToAction?.let {
        binding.adBtnCallToAction.text = it
    }
    nativeAd.body?.let {
        binding.adBodyText.text = it
    }
}
