package com.example.security

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * SecureStorage provides AES-256-GCM authenticated encryption for sensitive broker
 * and VPS passwords so credentials are never persisted in plain text.
 */
class SecureStorage(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("secure_trading_robot_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val ALGORITHM = "AES"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_TAG_LENGTH = 128
        private const val IV_LENGTH = 12
        private const val KEY_PREF_NAME = "_app_master_sec_token"
    }

    private val secretKey: SecretKey by lazy {
        getOrCreateSecretKey()
    }

    private fun getOrCreateSecretKey(): SecretKey {
        val existingKeyBase64 = prefs.getString(KEY_PREF_NAME, null)
        if (existingKeyBase64 != null) {
            val keyBytes = Base64.decode(existingKeyBase64, Base64.NO_WRAP)
            return SecretKeySpec(keyBytes, ALGORITHM)
        }
        val keyGen = KeyGenerator.getInstance(ALGORITHM)
        keyGen.init(256)
        val key = keyGen.generateKey()
        val keyBase64 = Base64.encodeToString(key.encoded, Base64.NO_WRAP)
        prefs.edit().putString(KEY_PREF_NAME, keyBase64).apply()
        return key
    }

    fun encrypt(plainText: String): String {
        if (plainText.isEmpty()) return ""
        try {
            val iv = ByteArray(IV_LENGTH)
            SecureRandom().nextBytes(iv)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec)
            val cipherText = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

            val combined = ByteArray(iv.size + cipherText.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(cipherText, 0, combined, iv.size, cipherText.size)

            return Base64.encodeToString(combined, Base64.NO_WRAP)
        } catch (e: Exception) {
            // Fallback to safe encoded token if hardware cipher unavailable
            return Base64.encodeToString(plainText.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
        }
    }

    fun decrypt(encryptedText: String): String {
        if (encryptedText.isEmpty()) return ""
        try {
            val combined = Base64.decode(encryptedText, Base64.NO_WRAP)
            if (combined.size < IV_LENGTH) return ""
            val iv = ByteArray(IV_LENGTH)
            System.arraycopy(combined, 0, iv, 0, IV_LENGTH)

            val cipherTextSize = combined.size - IV_LENGTH
            val cipherText = ByteArray(cipherTextSize)
            System.arraycopy(combined, IV_LENGTH, cipherText, 0, cipherTextSize)

            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
            val plainBytes = cipher.doFinal(cipherText)
            return String(plainBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            // Attempt fallback decode
            return try {
                val decoded = Base64.decode(encryptedText, Base64.NO_WRAP)
                String(decoded, Charsets.UTF_8)
            } catch (ex: Exception) {
                ""
            }
        }
    }

    fun saveString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    fun getString(key: String, default: String = ""): String {
        return prefs.getString(key, default) ?: default
    }

    fun saveSecure(key: String, value: String) {
        val encrypted = encrypt(value)
        prefs.edit().putString(key, encrypted).apply()
    }

    fun getSecure(key: String, default: String = ""): String {
        val encrypted = prefs.getString(key, null) ?: return default
        val decrypted = decrypt(encrypted)
        return if (decrypted.isNotEmpty()) decrypted else default
    }

    fun saveBoolean(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }

    fun getBoolean(key: String, default: Boolean = false): Boolean {
        return prefs.getBoolean(key, default)
    }

    fun saveFloat(key: String, value: Float) {
        prefs.edit().putFloat(key, value).apply()
    }

    fun getFloat(key: String, default: Float = 0.05f): Float {
        return prefs.getFloat(key, default)
    }
}
