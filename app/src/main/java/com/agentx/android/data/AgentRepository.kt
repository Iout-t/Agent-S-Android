package com.agentx.android.data

import com.agentx.android.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class AgentRepository(
    private val baseUrl: String = BuildConfig.DEFAULT_BACKEND_URL,
    private val client: OkHttpClient = OkHttpClient()
) {
    suspend fun startRun(instruction: String): RunResult = withContext(Dispatchers.IO) {
        val body = JSONObject().put("instruction", instruction).toString()
            .toRequestBody("application/json".toMediaType())
        val request = Request.Builder().url("$baseUrl/v1/runs").post(body).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("HTTP ${response.code}")
            val json = JSONObject(response.body?.string().orEmpty())
            RunResult(json.getString("id"), json.getString("status"))
        }
    }
}

data class RunResult(val id: String, val status: String)
