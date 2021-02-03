package com.edxavier.cerberus_sms.ui

import android.os.Bundle
import android.transition.Explode
import android.transition.Fade
import android.transition.Slide
import android.util.Log
import android.view.Window
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.edxavier.cerberus_sms.R
import com.edxavier.cerberus_sms.adapters.ContactAdapter
import com.edxavier.cerberus_sms.adapters.PhoneNumbersAdapter
import com.edxavier.cerberus_sms.data.repositories.RepoContact
import com.edxavier.cerberus_sms.databinding.ActivityContactDetailsBinding
import com.nicrosoft.consumoelectrico.ScopeActivity
import kotlinx.coroutines.launch

class ContactDetailsActivity : ScopeActivity() {

    private lateinit var binding: ActivityContactDetailsBinding
    lateinit var adapter: PhoneNumbersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        with(window) {
            requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
            requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
            allowEnterTransitionOverlap
            allowReturnTransitionOverlap
            exitTransition = Fade()
            enterTransition = Fade()
        }
        binding = ActivityContactDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        postponeEnterTransition()
        setupActivity()

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
}