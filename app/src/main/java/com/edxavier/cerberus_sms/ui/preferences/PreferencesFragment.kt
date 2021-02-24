package com.edxavier.cerberus_sms.ui.preferences

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
                Prefs.putString(setCode, text.toString())
            }
            positiveButton(text = "Ok")
        }
    }
}