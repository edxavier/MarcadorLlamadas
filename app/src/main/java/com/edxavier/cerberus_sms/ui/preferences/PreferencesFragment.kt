package com.edxavier.cerberus_sms.ui.preferences

import android.app.role.RoleManager
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.telecom.TelecomManager
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.edxavier.cerberus_sms.R
import com.edxavier.cerberus_sms.databinding.FragmentCallsBinding
import com.edxavier.cerberus_sms.databinding.FragmentPreferencesBinding
import com.pixplicity.easyprefs.library.Prefs

class PreferencesFragment : Fragment() {

    private lateinit var binding: FragmentPreferencesBinding
    val typeText = InputType.TYPE_CLASS_TEXT
    val typeNumber = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View{
        binding = FragmentPreferencesBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
    }

    private fun setupListeners() {
        binding.callLimitContainer.setOnClickListener {
            val defValue = Prefs.getInt("call_log_limit", 100)
            showDialog("Limite de registro de llamadas", "call_log_limit", defValue)
        }

        binding.rateAppContainer.setOnClickListener {
            val uri = Uri.parse("market://details?id=" + requireContext().packageName)
            val goToMarket = Intent(Intent.ACTION_VIEW, uri)
            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET or
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
            try {
                requireContext().startActivity(goToMarket)
            } catch (e: ActivityNotFoundException) {
                requireContext().startActivity(Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=" + requireContext().packageName)))
            }
        }
        binding.shareAppContainer.setOnClickListener {
            val i = Intent(Intent.ACTION_SEND)
            i.type = "text/plain"
            i.putExtra(Intent.EXTRA_SUBJECT, requireContext().resources.getString(R.string.app_name))
            val sAux = "https://play.google.com/store/apps/details?id=" + requireContext().packageName + " \n\n"
            i.putExtra(Intent.EXTRA_TEXT, sAux)
            requireContext().startActivity(Intent.createChooser(i, "Compartir"))
        }
        binding.defaultAppContainer.setOnClickListener {
            if (requireContext().getSystemService(TelecomManager::class.java).defaultDialerPackage != requireContext().packageName)
                requestRole()
            else
                Toast.makeText(requireContext(),
                    "Aplicación  ya fue establecida como aplicación por defecto de llamadas",
                    Toast.LENGTH_LONG).show()
        }
    }

    private fun showDialog(title:String, setCode:String, default:String){
        MaterialDialog(requireActivity()).show {
            title(text = title)
            input(prefill = default, inputType = typeText){ dialog, text ->
                Prefs.putString(setCode, text.toString())
            }
            positiveButton(text = "Ok")
        }
    }
    private fun showDialog(title:String, setCode:String, default:Int){
        MaterialDialog(requireActivity()).show {
            title(text = title)
            input(prefill = default.toString(), inputType = typeNumber){ dialog, text ->
                Prefs.putInt(setCode, text.toString().toInt())
            }
            positiveButton(text = "Ok")
        }
    }

    private fun requestRole() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            val roleManager = requireContext().getSystemService(AppCompatActivity.ROLE_SERVICE) as RoleManager
            val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER)
            requireActivity().startActivityForResult(intent, 1)
        }else {
            val changeDialer = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER)
            changeDialer.putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, requireContext().packageName)
            requireActivity().startActivity(changeDialer)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }
}