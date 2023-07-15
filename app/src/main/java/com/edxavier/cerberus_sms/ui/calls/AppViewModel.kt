package com.edxavier.cerberus_sms.ui.calls

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.edxavier.cerberus_sms.data.models.CallsLog
import com.edxavier.cerberus_sms.data.models.Contact
import com.edxavier.cerberus_sms.data.repositories.RepoContact
import com.edxavier.cerberus_sms.ui.core.states.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AppViewModel(private val repo: RepoContact): ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    var selectedCall: CallsLog = CallsLog()
    var selectedContact: Contact = Contact()
    var expandedFab by mutableStateOf(true)
    var firstVisible by mutableStateOf(0)

    suspend fun getCallLog(){
        _uiState.update { state ->
            state.copy(isLoading = true)
        }
        val calls = repo.getCallLog()
        _uiState.update { state ->
            state.copy(isLoading = false, callLog = calls)
        }
    }

    fun onDialPadEvent(){
        _uiState.update { state ->
            state.copy(dialShown = !uiState.value.dialShown)
        }
    }
    fun setCallNumberIntent(number:String){
        _uiState.update { state ->
            state.copy(
                dialShown = true,
                dialNumber = number
            )
        }
    }
    suspend fun getDialRecords(searchText: String = ""){
        _uiState.update { state ->
            state.copy(isLoading = true)
        }
        val contacts = repo.getContactNumbers(searchText = searchText)
        val calls = repo.getCallLog(searchText)
        _uiState.update { state ->
            state.copy(
                isLoading = false,
                dialCalls = calls,
                dialContacts = contacts
            )
        }
    }
    fun setEmptyDialSearch(){
        _uiState.update { state ->
            state.copy(
                isLoading = false,
                dialCalls = listOf(),
                dialContacts = listOf()
            )
        }
    }
    suspend fun getContacts(searchText: String = "", justFavorites: Boolean = false){
        _uiState.update { state ->
            state.copy(isLoading = true)
        }
        val contacts = repo.getContacts(searchText, justFavorites)
        _uiState.update { state ->
            state.copy(isLoading = false, contacts = contacts)
        }
    }
    fun deleteCallsForNumber(number: String? = null){
        repo.deleteNumberCallLog(number)
    }

    suspend fun getCallsFor(number: String){
        _uiState.update { state ->
            state.copy(isLoading = true, callLogForNumber = listOf())
        }
        val calls = repo.getCallLogFor(number)
        _uiState.update { state ->
            state.copy(isLoading = false, callLogForNumber = calls)
        }
    }
    fun blockNumber(number: String){
        repo.blockNumber(number)
    }
    fun setContactAsFav(fav: Boolean, cId: Int){
        repo.setContactAsFav(fav, cId)
    }

    suspend fun getContactNumbers(contactID:Int){
        val contacts = repo.getContactNumbers(contactID)
        _uiState.update { state ->
            state.copy(isLoading = false, contactNumbers = contacts)
        }
    }
}