package com.agentx.android

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.agentx.android.data.AgentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CredentialRequest(
    val appLabel: String,
    val reason: String,
    val savedAvailable: Boolean = false
)

data class AgentUiState(
    val running: Boolean = false,
    val message: String = "Ready",
    val searchUrl: String? = null,
    val query: String? = null,
    val credentialRequest: CredentialRequest? = null,
    val installedApp: InstalledApp? = null
)

class AgentViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AgentRepository()
    private val credentialStore = CredentialStore(application)
    private val executor = AppTaskExecutor(application)
    private val deviceExecutor = DeviceAutomationExecutor(application)
    private val _state = MutableStateFlow(AgentUiState())
    val state: StateFlow<AgentUiState> = _state.asStateFlow()
    private var pendingPlan: TaskPlan? = null

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
                    message = "Search ready: ${plan.query}",
                    searchUrl = plan.url,
                    query = plan.query
                )
            }

            TaskKind.FORM_TASK -> {
                _state.value = AgentUiState(
                    message = "Google Form opened. Review answers before submitting.",
                    searchUrl = plan.url,
                    query = plan.instruction
                )
            }

            TaskKind.DEVICE_TASK -> {
                val result = deviceExecutor.execute(plan)
                _state.value = AgentUiState(message = result.message)
            }

            TaskKind.APP_TASK -> {
                val savedAvailable = plan.requestedApp?.let(credentialStore::has) == true
                if (plan.requiresCredentials && !savedAvailable) {
                    pendingPlan = plan
                    _state.value = AgentUiState(
                        message = "Credentials needed before continuing",
                        query = plan.query,
                        credentialRequest = CredentialRequest(
                            appLabel = plan.requestedApp ?: "the requested app",
                            reason = plan.credentialReason ?: "Provide credentials for this run.",
                            savedAvailable = false
                        )
                    )
                } else {
                    executeAppTask(plan, credentialsProvided = savedAvailable)
                }
            }

            TaskKind.BACKEND_RUN -> runBackend(plan.instruction)
        }
    }

    fun continueWithCredentials(username: String, password: String, saveForFuture: Boolean) {
        val plan = pendingPlan ?: return
        if (username.isBlank() || password.isBlank()) {
            _state.value = _state.value.copy(message = "Enter both fields to continue")
            return
        }
        if (saveForFuture) {
            plan.requestedApp?.let { credentialStore.save(it, username, password) }
        }
        pendingPlan = null
        executeAppTask(plan, credentialsProvided = true)
    }

    fun cancelCredentialRequest() {
        pendingPlan = null
        _state.value = AgentUiState(message = "Credential request cancelled")
    }

    private fun executeAppTask(plan: TaskPlan, credentialsProvided: Boolean = false) {
        val result = executor.execute(plan)
        _state.value = AgentUiState(
            message = if (credentialsProvided) {
                if (credentialStore.has(plan.requestedApp.orEmpty())) {
                    "Using encrypted saved credentials for this run. ${result.message}"
                } else {
                    "Credentials received for this run. ${result.message}"
                }
            } else {
                result.message
            },
            searchUrl = result.fallbackUrl,
            query = plan.query,
            installedApp = result.installed
        )
    }

    private fun runBackend(instruction: String) {
        viewModelScope.launch {
            _state.value = AgentUiState(true, "Sending task to DailyDay backend…")
            _state.value = try {
                val result = repository.startRun(instruction)
                AgentUiState(false, "Run ${result.id}: ${result.status}")
            } catch (error: Exception) {
                AgentUiState(false, "Backend error: ${error.message ?: "unknown error"}")
            }
        }
    }
}
