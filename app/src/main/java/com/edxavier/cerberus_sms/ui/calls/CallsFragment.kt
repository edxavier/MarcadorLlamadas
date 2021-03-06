package com.edxavier.cerberus_sms.ui.calls

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.telephony.PhoneNumberUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.edxavier.cerberus_sms.R
import com.edxavier.cerberus_sms.adapters.CallLogAdapter
import com.edxavier.cerberus_sms.data.models.CallsLog
import com.edxavier.cerberus_sms.data.models.Operator
import com.edxavier.cerberus_sms.data.repositories.RepoContact
import com.edxavier.cerberus_sms.data.repositories.RepoOperator
import com.edxavier.cerberus_sms.databinding.FragmentCallsBinding
import com.edxavier.cerberus_sms.helpers.*
import com.nicrosoft.consumoelectrico.ScopeFragment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import java.util.*


class CallsFragment : ScopeFragment() {

    private lateinit var binding: FragmentCallsBinding
    private val CALL_PERMISSION_REQUEST = 1
    lateinit var adapter: CallLogAdapter

    private val enteredNumber = MutableStateFlow("")

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
        listenForTextChanges()
        handleCallIntent()
    }

    private fun handleCallIntent() {
        if (!requireActivity().intent.dataString.isNullOrEmpty()){
            val callPhone = PhoneNumberUtils.getNumberFromIntent(requireActivity().intent, requireContext()).clearPhoneString()
            callPhone.forEachIndexed { index, char ->
                //Log.e("EDER", "$index $char")
                if(index>0 && char.toString() != "+") {
                    insertChar(char.toString())
                }else if (index==0){
                    insertChar(char.toString())
                }
            }
            binding.dialPad.visible()
            binding.showDialPad.invisible()
        }
    }

    private fun checkRequiredPermission(){
        if (!hasReadCallLogPermission()) {
            binding.btnPermissionAssign.visible()
            binding.notificationMessage.text = "No fue posible acceder a sus registros de llamada"
        }
    }


    private fun hasReadCallLogPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_CALL_LOG
        ) == PackageManager.PERMISSION_GRANTED
    }
    private fun hasCallPhonePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED
    }
    private fun requestReadCallLogPermission() {
        requestPermissions(
            arrayOf(
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.CALL_PHONE
            ), this.CALL_PERMISSION_REQUEST
        )
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
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


    private fun listenForTextChanges(){
        launch {
            enteredNumber.debounce(350).collect {
                if(it.length >=5){
                    val repo = RepoOperator.getInstance(requireContext())
                    val op = repo.getOperator(it)
                    if (op!=null){
                        when (op.operator) {
                            Operator.INTERNATIONAL -> {
                                binding.padOperator.text  = if(op.area.isNotBlank())
                                    "${op.area}, ${op.country}"
                                else
                                    op.country
                                op.operator.getOperatorString()
                            }
                            Operator.CONVENTIONAL -> {
                                binding.padOperator.text  = if(op.area.isNotBlank())
                                    "${op.area}, ${op.country}"
                                else
                                    op.country
                                op.operator.getOperatorString()
                            }
                            else -> binding.padOperator.text = op.operator.getOperatorString()
                        }
                        binding.padOperator.setTextColor(op.operator.getOperatorColor(requireContext()))
                    }else
                        binding.padOperator.text = ""
                }else
                    binding.padOperator.text = ""
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
        binding.recyclerCallLog.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )
        binding.recyclerCallLog.adapter = adapter
        binding.recyclerCallLog.setHasFixedSize(true)

        if(hasReadCallLogPermission()){
            getCallsLog()
        }

        with(binding){

            dialNumber.showSoftInputOnFocus = false
            showDialPad.setOnClickListener {
                val animSlideUp: Animation = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up)
                val animSlideDown: Animation = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_down)

                dialPad.startAnimation(animSlideUp)
                dialPad.visible()
                showDialPad.startAnimation(animSlideDown)
                showDialPad.invisible()
            }
            hideDialPad.setOnClickListener {
                val animSlideUp: Animation = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up)
                val animSlideDown: Animation = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_down)
                dialPad.startAnimation(animSlideDown)
                dialPad.invisible()
                showDialPad.startAnimation(animSlideUp)
                showDialPad.visible()
            }
            btnBackspace.setOnLongClickListener {
                dialNumber.setText("")
                enteredNumber.value = binding.dialNumber.text.toString()
                true
            }
            btnBackspace.setOnClickListener {
                val cursorPos = binding.dialNumber.selectionStart - 1
                if(cursorPos>=0) {
                    val tmp = binding.dialNumber.text.toString().removeRange(
                        cursorPos,
                        cursorPos + 1
                    )
                    binding.dialNumber.setText(tmp)
                    binding.dialNumber.setSelection(cursorPos)
                    enteredNumber.value = binding.dialNumber.text.toString()
                }
            }
            btnCall.setOnClickListener {
                if (dialNumber.text.isNotEmpty() && hasCallPhonePermission())
                    requireContext().makeCall(dialNumber.text.toString())
                else
                    Toast.makeText(
                        requireContext(),
                        "No se pudo realizar la llamada.",
                        Toast.LENGTH_LONG
                    ).show()
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
        enteredNumber.value = binding.dialNumber.text.toString()
    }

}