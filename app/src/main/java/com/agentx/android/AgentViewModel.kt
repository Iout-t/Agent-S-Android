package com.agentx.android

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agentx.android.data.AgentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AgentUiState(
    val running: Boolean = false,
    val message: String = "Ready",
    val searchUrl: String? = null,
    val query: String? = null
)

class AgentViewModel(private val repository: AgentRepository = AgentRepository()) : ViewModel() {
    private val _state = MutableStateFlow(AgentUiState())
    val state: StateFlow<AgentUiState> = _state.asStateFlow()

    fun run(instruction: String) {
        val plan = try {
            TaskPlanner.plan(instruction)
        } catch (error: IllegalArgumentException) {
            _state.value = AgentUiState(message = error.message ?: "Enter an instruction first")
            return
        }

        when (plan.kind) {
            TaskKind.WEB_SEARCH -> {
                _state.value = AgentUiState(
                    running = false,
                    message = "Search ready: ${plan.query}",
                    searchUrl = plan.url,
                    query = plan.query
                )
            }

            TaskKind.BACKEND_RUN -> runBackend(plan.instruction)
        }
    }

    private fun runBackend(instruction: String) {
        viewModelScope.launch {
            _state.value = AgentUiState(true, "Sending task to Agent X backend…")
            _state.value = try {
                val result = repository.startRun(instruction)
                AgentUiState(false, "Run ${result.id}: ${result.status}")
            } catch (error: Exception) {
                AgentUiState(false, "Backend error: ${error.message ?: "unknown error"}")
            }
        }
    }
}
