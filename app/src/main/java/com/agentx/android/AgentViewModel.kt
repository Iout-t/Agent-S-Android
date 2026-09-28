package com.agentx.android

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agentx.android.data.AgentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AgentUiState(val running: Boolean = false, val message: String = "Ready")

class AgentViewModel(private val repository: AgentRepository = AgentRepository()) : ViewModel() {
    private val _state = MutableStateFlow(AgentUiState())
    val state: StateFlow<AgentUiState> = _state.asStateFlow()

    fun run(instruction: String) {
        viewModelScope.launch {
            _state.value = AgentUiState(true, "Sending instruction to Python core…")
            _state.value = try {
                val result = repository.startRun(instruction)
                AgentUiState(false, "Run ${result.id}: ${result.status}")
            } catch (error: Exception) {
                AgentUiState(false, "Backend error: ${error.message ?: "unknown error"}")
            }
        }
    }
}
