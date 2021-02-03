package com.edxavier.cerberus_sms.ui.calls

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.edxavier.cerberus_sms.R
import com.edxavier.cerberus_sms.databinding.ActivityMainBinding
import com.edxavier.cerberus_sms.databinding.FragmentCallsBinding
import com.edxavier.cerberus_sms.helpers.invisible
import com.edxavier.cerberus_sms.helpers.visible
import com.google.android.material.snackbar.Snackbar

class CallsFragment : Fragment() {

    private lateinit var binding: FragmentCallsBinding
    private val CALL_PERMISSION_REQUEST = 1

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
        binding.btnPermissionAssign.setOnClickListener { requestReadCallLogPermission() }
        checkRequiredPermission()
    }

    private fun checkRequiredPermission(){
        if (!hasReadCallLogPermission()) {
            binding.btnPermissionAssign.visible()
            binding.notificationMessage.text = "No fue posible acceder a sus registros de llamada"
        }else {
            binding.notificationMessage.text = "No hay registros de llamada para mostrar"
            binding.btnPermissionAssign.invisible()
        }
    }


    private fun hasReadCallLogPermission(): Boolean {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED
    }
    private fun requestReadCallLogPermission() {
        requestPermissions(arrayOf(Manifest.permission.READ_CALL_LOG), this.CALL_PERMISSION_REQUEST)
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            this.CALL_PERMISSION_REQUEST -> {
                checkRequiredPermission()
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(requireContext(), "PERMISO DENEGADO", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(requireContext(), "PERMISO CONCEDIDO", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

}