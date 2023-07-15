package com.edxavier.cerberus_sms.ui.contacts

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.edxavier.cerberus_sms.R
import com.edxavier.cerberus_sms.adapters.ContactAdapter
import com.edxavier.cerberus_sms.data.repositories.RepoContact
import com.edxavier.cerberus_sms.databinding.FragmentContactsBinding
import com.edxavier.cerberus_sms.helpers.invisible
import com.edxavier.cerberus_sms.helpers.visible
import com.nicrosoft.consumoelectrico.ScopeFragment
import kotlinx.coroutines.launch

class ContactsFragment : ScopeFragment() {

    private val CONTACTS_PERMISSION_REQUEST = 2
    private lateinit var binding: FragmentContactsBinding
    lateinit var adapter: ContactAdapter

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = FragmentContactsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.btnPermissionAssign.setOnClickListener { requestReadContactsPermission() }
        checkRequiredPermission()
        adapter = ContactAdapter(requireContext(), requireActivity())
        binding.recyclerContacts.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.recyclerContacts.adapter = adapter
        binding.recyclerContacts.setHasFixedSize(true)
        if(hasReadContactsPermission()) {
            launch {
                binding.notificationAnimation.setAnimation(R.raw.loading)
                binding.notificationAnimation.playAnimation()
                binding.notificationAnimation.loop(true)
                binding.notificationMessage.text = "Cargando..."
                val repo = RepoContact.getInstance(requireContext())
                val res = repo.getContacts()
                adapter.submitList(res)
                binding.recyclerContacts.adapter = adapter
                binding.notificationContainer.invisible()
            }
        }

    }

    private fun checkRequiredPermission(){
        if (!hasReadContactsPermission()) {
            binding.btnPermissionAssign.visible()
            binding.notificationMessage.text = "No fue posible acceder a su lista de contactos"
        }else {
            binding.notificationMessage.text = "No hay contactos para mostrar"
            binding.btnPermissionAssign.invisible()
        }
    }

    private fun hasReadContactsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
    }
    private fun requestReadContactsPermission() {
        requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), this.CONTACTS_PERMISSION_REQUEST)
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            this.CONTACTS_PERMISSION_REQUEST -> {
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