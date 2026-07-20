package com.edxavier.cerberus_sms.ui.calls

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edxavier.cerberus_sms.data.models.CallsLog
import com.edxavier.cerberus_sms.data.models.Contact
import com.edxavier.cerberus_sms.data.repositories.RepoContact
import com.edxavier.cerberus_sms.helpers.AnalyticsLogger
import com.edxavier.cerberus_sms.ui.core.states.UiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AppViewModel(private val repo: RepoContact): ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    var selectedCall: CallsLog = CallsLog()
    var selectedContact: Contact = Contact()
    var expandedFab by mutableStateOf(true)
    var firstVisible by mutableStateOf(0)

    private var isLoadingMore = false

    suspend fun getCallLog(){
        _uiState.update { state ->
            state.copy(isLoading = true)
        }
        val (calls, hasMore) = repo.getCallLog(beforeDate = null)
        _uiState.update { state ->
            state.copy(isLoading = false, callLog = calls, hasMoreCallLog = hasMore)
        }
    }

    fun refreshCallLog() {
        repo.invalidateCallLogCache()
        viewModelScope.launch {
            delay(1500)
            getCallLog()
        }
    }

    fun loadMoreCallLog() {
        if (isLoadingMore || uiState.value.isLoading || !uiState.value.hasMoreCallLog) return
        val lastCall = uiState.value.callLog.lastOrNull() ?: return
        isLoadingMore = true
        viewModelScope.launch {
            val (more, hasMore) = repo.getCallLog(beforeDate = lastCall.callDate.timeInMillis)
            _uiState.update { state ->
                state.copy(
                    callLog = state.callLog + more,
                    hasMoreCallLog = hasMore
                )
            }
            isLoadingMore = false
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
        val (calls, _) = repo.getCallLog(searchText)
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
        AnalyticsLogger.blockNumber()
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