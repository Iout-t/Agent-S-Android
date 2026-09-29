package com.agentx.android

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Stores credentials only when the user explicitly opts in. Values are encrypted by Android Keystore. */
class CredentialStore(context: Context) {
    data class Credentials(val username: String, val password: String)

    private val preferences = context.applicationContext.getSharedPreferences(
        "dailyday_credentials",
        Context.MODE_PRIVATE
    )

    fun save(appLabel: String, username: String, password: String) {
        val payload = JSONObject()
            .put("username", username)
            .put("password", password)
            .toString()
            .toByteArray(StandardCharsets.UTF_8)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key())
        val encrypted = cipher.doFinal(payload)
        val encoded = Base64.encodeToString(encrypted, Base64.NO_WRAP)
        val iv = Base64.encodeToString(cipher.iv, Base64.NO_WRAP)
        preferences.edit()
            .putString(storageKey(appLabel), "$iv:$encoded")
            .apply()
    }

    fun load(appLabel: String): Credentials? {
        val stored = preferences.getString(storageKey(appLabel), null) ?: return null
        return runCatching {
            val separator = stored.indexOf(':')
            require(separator > 0)
            val iv = Base64.decode(stored.substring(0, separator), Base64.NO_WRAP)
            val encrypted = Base64.decode(stored.substring(separator + 1), Base64.NO_WRAP)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, iv))
            val json = JSONObject(String(cipher.doFinal(encrypted), StandardCharsets.UTF_8))
            Credentials(json.getString("username"), json.getString("password"))
        }.getOrNull()
    }

    fun has(appLabel: String): Boolean = load(appLabel) != null

    private fun storageKey(appLabel: String): String = "credential_${appLabel.lowercase().trim()}"

    private fun key(): SecretKey {
        val keyStore = java.security.KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
            generator.init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setUserAuthenticationRequired(false)
                    .build()
            )
            generator.generateKey()
        }
        return (keyStore.getKey(KEY_ALIAS, null) as SecretKey)
    }

    private companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val KEY_ALIAS = "dailyday_credentials_key"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
    }
}
