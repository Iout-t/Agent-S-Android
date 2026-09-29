package com.agentx.android

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.app.SearchManager
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

internal fun spotifyContentUrl(query: String): String =
    "https://open.spotify.com/search/${Uri.encode(query)}"

data class InstalledApp(
    val label: String,
    val packageName: String
)

data class AppExecutionResult(
    val launched: Boolean,
    val installed: InstalledApp? = null,
    val fallbackUrl: String? = null,
    val message: String
)

class AppTaskExecutor(context: Context) {
    private val appContext = context.applicationContext
    private val packageManager = appContext.packageManager

    fun findCompatibleApp(requestedApp: String?, preferredPackage: String?): InstalledApp? {
        preferredPackage?.let { packageName ->
            try {
                val appInfo = packageManager.getApplicationInfo(packageName, 0)
                return InstalledApp(
                    label = appInfo.loadLabel(packageManager).toString(),
                    packageName = packageName
                )
            } catch (_: PackageManager.NameNotFoundException) {
                // Continue with launcher-label matching for aliases and user-installed apps.
            }
        }
        val launchIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val candidates = packageManager.queryIntentActivities(
            launchIntent,
            PackageManager.MATCH_DEFAULT_ONLY
        )
        val matching = candidates.mapNotNull { resolveInfo ->
            val packageName = resolveInfo.activityInfo?.packageName ?: return@mapNotNull null
            val label = resolveInfo.loadLabel(packageManager).toString()
            InstalledApp(label = label, packageName = packageName)
        }
        return matching.firstOrNull { it.packageName == preferredPackage }
            ?: matching.firstOrNull {
                requestedApp != null && (
                    it.label.equals(requestedApp, ignoreCase = true) ||
                        it.label.contains(requestedApp, ignoreCase = true) ||
                        requestedApp.contains(it.label, ignoreCase = true)
                    )
            }
    }

    fun execute(plan: TaskPlan): AppExecutionResult {
        val installed = findCompatibleApp(plan.requestedApp, plan.preferredPackage)
        val fallbackUrl = plan.query?.let { query -> TaskPlanner.appWebUrl(plan.requestedApp, query) }

        if (installed == null) {
            return AppExecutionResult(
                launched = false,
                fallbackUrl = fallbackUrl,
                message = "${plan.requestedApp ?: "Requested app"} is not installed. No compatible app was found."
            )
        }

        val intent = createIntent(plan, installed)
        return try {
            appContext.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            AppExecutionResult(
                launched = true,
                installed = installed,
                fallbackUrl = null,
                message = "Opened ${installed.label}: ${plan.actionDescription}"
            )
        } catch (error: Exception) {
            AppExecutionResult(
                launched = false,
                installed = installed,
                fallbackUrl = fallbackUrl,
                message = "Could not complete the task in ${installed.label}. The app is installed, but it does not expose a compatible action."
            )
        }
    }

    private fun createIntent(plan: TaskPlan, installed: InstalledApp): Intent {
        if (plan.query != null && installed.packageName == "com.google.android.youtube") {
            return Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.youtube.com/results?search_query=${encode(plan.query)}")
            ).setPackage(installed.packageName)
        }
        if (plan.query != null && installed.packageName == "com.spotify.music") {
            return Intent(
                Intent.ACTION_VIEW,
                Uri.parse(spotifyContentUrl(plan.query))
            ).setPackage(installed.packageName)
        }
        if (plan.query != null && installed.packageName == "com.google.android.apps.maps") {
            return Intent(
                Intent.ACTION_VIEW,
                Uri.parse("geo:0,0?q=${encode(plan.query)}")
            ).setPackage(installed.packageName)
        }

        if (plan.query != null) {
            val searchIntent = Intent(Intent.ACTION_SEARCH).apply {
                setPackage(installed.packageName)
                putExtra(SearchManager.QUERY, plan.query)
            }
            if (searchIntent.resolveActivity(packageManager) != null) return searchIntent
        }

        return packageManager.getLaunchIntentForPackage(installed.packageName)
            ?: Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).setPackage(installed.packageName)
    }

    private fun encode(value: String): String =
        URLEncoder.encode(value, StandardCharsets.UTF_8.toString())

}
