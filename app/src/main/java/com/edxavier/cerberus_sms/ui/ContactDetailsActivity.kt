package com.edxavier.cerberus_sms.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import coil.transform.RoundedCornersTransformation
import com.edxavier.cerberus_sms.R
import com.edxavier.cerberus_sms.adapters.PhoneNumbersAdapter
import com.edxavier.cerberus_sms.data.repositories.RepoContact
import com.edxavier.cerberus_sms.databinding.ActivityContactDetailsBinding
import com.edxavier.cerberus_sms.databinding.AdNativeLayoutBinding
import com.edxavier.cerberus_sms.helpers.visible
import com.google.android.gms.ads.*
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.material.button.MaterialButton
import com.nicrosoft.consumoelectrico.ScopeActivity
import kotlinx.android.synthetic.main.ad_native_layout.*
import kotlinx.coroutines.launch

class ContactDetailsActivity : ScopeActivity() {

    private lateinit var binding: ActivityContactDetailsBinding
    lateinit var adapter: PhoneNumbersAdapter
    lateinit var nativeAd:NativeAd

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*with(window) {
            requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
            requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
            allowEnterTransitionOverlap
            allowReturnTransitionOverlap
            exitTransition = Fade()
            enterTransition = Fade()
        }
         */
        binding = ActivityContactDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        postponeEnterTransition()
        setupActivity()
        MobileAds.initialize(this){}
        launch {
            loadNativeAd()
        }
    }
    private fun setupActivity() {
        val contactId = intent.getIntExtra("contact_id", -100)
        val contactName = intent.getStringExtra("contact_name")
        val contactPhoto = intent.getStringExtra("contact_photo")

        binding.collapsingToolbar.title = contactName
        binding.expandedImage.load(contactPhoto){
            error(R.drawable.diente_leon)
            crossfade(true)
            placeholder(R.drawable.diente_leon)
        }
        adapter = PhoneNumbersAdapter(this, this)
        binding.recyclerContactNumbers.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.recyclerContactNumbers.adapter = adapter
        binding.recyclerContactNumbers.setHasFixedSize(true)
        launch {
            val repo = RepoContact.getInstance(this@ContactDetailsActivity)
            adapter.submitList(repo.getContactNumbers(contactId))
        }
    }
    override fun onBackPressed() {
        super.onBackPressed()
        supportFinishAfterTransition()
    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    @SuppressLint("InflateParams")
    private fun loadNativeAd(){
        val requestConfig = RequestConfiguration.Builder()
                .setTestDeviceIds(arrayOf(
                        "AC5F34885B0FE7EF03A409EB12A0F949",
                        AdRequest.DEVICE_ID_EMULATOR
                ).toList())
                .build()
        MobileAds.setRequestConfiguration(requestConfig)
        val nativeCode = "ca-app-pub-9964109306515647/3495890674"
        val builder = AdLoader.Builder(this, nativeCode)

        builder.forNativeAd { native ->
            if (isDestroyed || isFinishing || isChangingConfigurations) {
                nativeAd.destroy();
                return@forNativeAd;
            }
            nativeAd = native
            val adBinding = AdNativeLayoutBinding.inflate(layoutInflater)
            //val nativeAdview = AdNativeLayoutBinding.inflate(layoutInflater).root
            binding.nativeAdFrameLayout.removeAllViews()
            binding.nativeAdFrameLayout.addView(populateNativeAd(nativeAd, adBinding))
        }

        val adLoader = builder.withAdListener(object : AdListener(){
            override fun onAdFailedToLoad(p0: LoadAdError?) {
                super.onAdFailedToLoad(p0)
                Log.e("EDER", "${p0?.message}: ${p0?.cause.toString()}")
            }
        }).build()
        adLoader.loadAd(AdRequest.Builder().build())
    }

    private fun populateNativeAd(nativeAd:NativeAd, adView: AdNativeLayoutBinding): NativeAdView{
        val nativeAdView = adView.root
        with(adView){
            adHeadline.text = nativeAd.headline
            nativeAdView.headlineView = adHeadline

            nativeAd.mediaContent?.let {
                adMedia.setMediaContent(it)
                adMedia.visible()
                adMedia.setImageScaleType(ImageView.ScaleType.FIT_XY)
                nativeAdView.mediaView = adMedia
            }
            nativeAd.advertiser?.let {
                adAdvertiser.text = it
                adAdvertiser.visible()
                nativeAdView.advertiserView = adAdvertiser
            }
            nativeAd.icon?.let {
                adIcon.setImageDrawable(it.drawable)
                //adIcon.load(it.drawable){transformations(RoundedCornersTransformation(radius = 8f))}
                adIcon.visible()
                nativeAdView.iconView = adIcon
                //(nativeAdView.iconView as ImageView).load(it.drawable){transformations(RoundedCornersTransformation(radius = 8f))}
            }
            nativeAd.starRating?.let {
                adStartRating.rating = it.toFloat()
                adStartRating.visible()
                nativeAdView.starRatingView = adStartRating
            }
            nativeAd.callToAction?.let {
                adBtnCallToAction.text = it
                nativeAdView.callToActionView = adBtnCallToAction
            }
            nativeAd.body?.let {
                adBodyText.text = it
                nativeAdView.bodyView = adBodyText
            }
        }
        nativeAdView.setNativeAd(nativeAd)
        return nativeAdView
    }


    override fun onDestroy() {
        super.onDestroy()
    }
}