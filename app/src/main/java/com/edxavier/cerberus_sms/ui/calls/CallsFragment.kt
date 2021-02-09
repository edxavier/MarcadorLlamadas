package com.edxavier.cerberus_sms.ui.calls

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.edxavier.cerberus_sms.R
import com.edxavier.cerberus_sms.adapters.CallLogAdapter
import com.edxavier.cerberus_sms.data.models.CallsLog
import com.edxavier.cerberus_sms.data.repositories.RepoContact
import com.edxavier.cerberus_sms.databinding.FragmentCallsBinding
import com.edxavier.cerberus_sms.helpers.invisible
import com.edxavier.cerberus_sms.helpers.makeCall
import com.edxavier.cerberus_sms.helpers.toPhoneFormat
import com.edxavier.cerberus_sms.helpers.visible
import com.nicrosoft.consumoelectrico.ScopeFragment
import kotlinx.coroutines.launch
import java.util.*

class CallsFragment : ScopeFragment() {

    private lateinit var binding: FragmentCallsBinding
    private val CALL_PERMISSION_REQUEST = 1
    lateinit var adapter: CallLogAdapter

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        binding = FragmentCallsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initLayout()
    }

    private fun checkRequiredPermission(){
        if (!hasReadCallLogPermission()) {
            binding.btnPermissionAssign.visible()
            binding.notificationMessage.text = "No fue posible acceder a sus registros de llamada"
        }
    }


    private fun hasReadCallLogPermission(): Boolean {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED
    }
    private fun requestReadCallLogPermission() {
        requestPermissions(arrayOf(Manifest.permission.READ_CALL_LOG, Manifest.permission.CALL_PHONE), this.CALL_PERMISSION_REQUEST)
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            this.CALL_PERMISSION_REQUEST -> {
                checkRequiredPermission()
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(requireContext(), "PERMISO DENEGADO", Toast.LENGTH_LONG).show()
                } else {
                    getCallsLog()
                }
            }
        }
    }

    private fun getCallsLog(){
        binding.btnPermissionAssign.invisible()
        lifecycleScope.launch {
            binding.notificationAnimation.setAnimation(R.raw.loading)
            binding.notificationAnimation.playAnimation()
            binding.notificationAnimation.loop(true)
            binding.notificationMessage.text = "Cargando..."
            val repo = RepoContact.getInstance(requireContext())
            val calls = repo.getCallLog()
            val gby = calls.groupBy { it.number }
            val callsGrouped: MutableList<CallsLog> =  ArrayList()
            gby.forEach {
                val call = it.value[0]
                call.total = it.value.size
                callsGrouped.add(call)
            }

            adapter.submitList(callsGrouped)
            if (calls.isNotEmpty())
                binding.notificationContainer.invisible()
        }
    }
    private fun initLayout(){
        binding.btnPermissionAssign.setOnClickListener { requestReadCallLogPermission() }
        checkRequiredPermission()
        adapter = CallLogAdapter(requireContext(), requireActivity())
        binding.recyclerCallLog.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.recyclerCallLog.adapter = adapter
        binding.recyclerCallLog.setHasFixedSize(true)

        if(hasReadCallLogPermission()){
            getCallsLog()
        }

        with(binding){
            val cursorPos = binding.dialNumber.selectionStart

            dialNumber.showSoftInputOnFocus = false
            showDialPad.setOnClickListener {
                dialPad.visible()
                showDialPad.invisible()
            }
            hideDialPad.setOnClickListener {
                dialPad.invisible()
                showDialPad.visible()
            }
            btnBackspace.setOnLongClickListener {
                dialNumber.setText("")
                true
            }
            btnBackspace.setOnClickListener {
                val cursorPos = binding.dialNumber.selectionStart - 1
                if(cursorPos>=0) {
                    val tmp = binding.dialNumber.text.toString().removeRange(cursorPos, cursorPos + 1)
                    binding.dialNumber.setText(tmp)
                    binding.dialNumber.setSelection(cursorPos)
                }
            }
            btnCall.setOnClickListener {
                if (dialNumber.text.isNotEmpty())
                    requireContext().makeCall(dialNumber.text.toString())
                else
                    Toast.makeText(requireContext(), "No se pudo realizar la llamada.", Toast.LENGTH_LONG).show()
            }
            pad0.setOnLongClickListener {
                insertChar("+")
                true
            }
            pad0.setOnClickListener { insertChar("0") }
            pad1.setOnClickListener { insertChar("1") }
            pad2.setOnClickListener { insertChar("2") }
            pad3.setOnClickListener { insertChar("3") }
            pad4.setOnClickListener { insertChar("4") }
            pad5.setOnClickListener { insertChar("5") }
            pad6.setOnClickListener { insertChar("6") }
            pad7.setOnClickListener { insertChar("7") }
            pad8.setOnClickListener { insertChar("8") }
            pad9.setOnClickListener { insertChar("9") }
            padAsterisk.setOnClickListener { insertChar("*") }
            padHash.setOnClickListener { insertChar("#") }

        }
    }

    private fun insertChar(char: String){
        val cursorPos = binding.dialNumber.selectionStart
        binding.dialNumber.text.insert(cursorPos, char)
    }

}