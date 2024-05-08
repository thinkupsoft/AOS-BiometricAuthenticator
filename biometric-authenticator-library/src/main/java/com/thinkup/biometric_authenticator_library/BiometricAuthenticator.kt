package com.thinkup.biometric_authenticator_library

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.AuthenticationResult
import androidx.fragment.app.FragmentActivity
import com.thinkup.biometric_authenticator_library.models.BiometricAuthStatus
import com.thinkup.biometric_authenticator_library.models.BiometricAuthStatus.AVAILABLE_BUT_NOT_ENROLLED
import com.thinkup.biometric_authenticator_library.models.BiometricAuthStatus.NOT_AVAILABLE
import com.thinkup.biometric_authenticator_library.models.BiometricAuthStatus.READY
import com.thinkup.biometric_authenticator_library.models.BiometricAuthStatus.READY_NEEDS_SETUP
import com.thinkup.biometric_authenticator_library.models.BiometricAuthStatus.TEMPORARILY_NOT_AVAILABLE
import com.thinkup.biometric_authenticator_library.models.BiometricType
import com.thinkup.biometric_authenticator_library.models.BiometricType.LOGIN
import com.thinkup.biometric_authenticator_library.models.BiometricType.TRANSFER
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

/**
 * Created by Martin Zarzar on 1/4/24.
 */
class BiometricAuthenticator (val context: Context) {
    companion object {
        private const val ANDROID_KEY_STORE = "AndroidKeyStore"
        private const val BIOMETRIC_KEY = "BiometricKey"
        private const val BIOMETRIC_PREF = "BiometricPref"
        private const val PREF_KEY_LOGIN_DISABLED = "BiometricLoginDisabled"
        private const val PREF_KEY_TRANSFER_DISABLED = "BiometricTransferDisabled"
    }

    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(BIOMETRIC_PREF, Context.MODE_PRIVATE)
    }
    private val biometricManager = BiometricManager.from(context)

    private fun isBiometricAuthenticationAvailable(): BiometricAuthStatus {
        val canAuthenticate = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
        return when (canAuthenticate) {
            BiometricManager.BIOMETRIC_SUCCESS -> READY
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> NOT_AVAILABLE
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> TEMPORARILY_NOT_AVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> AVAILABLE_BUT_NOT_ENROLLED
            else -> NOT_AVAILABLE
        }
    }

    fun checkBiometricAvailability(prefKey: String, type: BiometricType): BiometricAuthStatus {
        val basicAuthStatus = isBiometricAuthenticationAvailable()
        if (basicAuthStatus == READY) {
            val hasTouchIdToken = getEncryptedToken(prefKey)
            val isBiometricDisabled = isBiometricDisabled(type)

            if (isBiometricDisabled) {
                return NOT_AVAILABLE
            }
            if (hasTouchIdToken.isNullOrEmpty()) {
                return READY_NEEDS_SETUP
            }
        }

        return basicAuthStatus
    }

    fun promptBiometricAuthentication(
        prefKey: String,
        title: String,
        subtitle: String,
        negativeButtonText: String,
        activity: FragmentActivity,
        resetKey: Boolean = false,
        onSuccess: (AuthenticationResult) -> Unit,
        onFailed: () -> Unit = {},
        onError: (errorCode: Int, errString: String) -> Unit = { _, _ -> },
        onCanceled: () -> Unit = {}
    ) {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText(negativeButtonText)
            .build()

        val biometricPrompt = BiometricPrompt(
            activity,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess(result)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onFailed()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                        onCanceled()
                    } else {
                        onError(errorCode, errString.toString())
                    }
                }
            }
        )

        if (resetKey) {
            deleteEncryptedToken(prefKey)
        }
        generateKey()
        val cipher = getCipher(prefKey)
        val cryptoObject = BiometricPrompt.CryptoObject(cipher)

        biometricPrompt.authenticate(promptInfo, cryptoObject)
    }

    private fun getCipher(prefKey: String): Cipher {
        val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
        val hasTouchIdToken = getEncryptedToken(prefKey)
        if (hasTouchIdToken.isNullOrEmpty()) {
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
        } else {
            val encryptedDataWithIv = Base64.decode(hasTouchIdToken, Base64.DEFAULT)
            val iv = encryptedDataWithIv.copyOfRange(0, 16)
            val secretKey = getSecretKey()
            cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))
        }
        return cipher
    }

    private fun generateKey() {
        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
        keyStore.load(null)

        if (!keyStore.containsAlias(BIOMETRIC_KEY)) {
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE)
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                BIOMETRIC_KEY,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            ).apply {
                setUserAuthenticationRequired(true)
                setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
            }.build()
            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()
        }
    }

    fun encryptToken(token: String, authResult: AuthenticationResult, prefKeyToken: String) {
        val cipher = authResult.cryptoObject?.cipher
        if (cipher != null) {
            val encryptedByteArray = cipher.doFinal(token.toByteArray())
            val ivAndEncryptedByteArray = cipher.iv + encryptedByteArray
            val tokenBase64 = Base64.encodeToString(ivAndEncryptedByteArray, Base64.DEFAULT)

            saveEncryptedToken(tokenBase64, prefKeyToken)
        }
    }

    fun decryptToken(authResult: AuthenticationResult, prefKey: String): String {
        val cipher = authResult.cryptoObject?.cipher
        var token = ""
        if (cipher != null) {
            val tokenBase64 = getEncryptedToken(prefKey)
            val encryptedByteArray = Base64.decode(tokenBase64, Base64.DEFAULT)
            val encrypted = encryptedByteArray.copyOfRange(16, encryptedByteArray.size)
            val decrypted = cipher.doFinal(encrypted)
            token = String(decrypted)
        }
        return token
    }

    private fun deleteEncryptedToken(prefKey: String) {
        sharedPreferences.edit().remove(prefKey).apply()
    }

    private fun getSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }
        return keyStore.getKey(BIOMETRIC_KEY, null) as SecretKey
    }

    private fun saveEncryptedToken(encryptedTokenBase64: String, prefKey: String) {
        sharedPreferences.edit().putString(prefKey, encryptedTokenBase64).apply()
    }

    private fun getEncryptedToken(prefKey: String): String? {
        return sharedPreferences.getString(prefKey, null)
    }

    fun setBiometricEnabled(isEnabled: Boolean, type: BiometricType) {
        sharedPreferences.edit().putBoolean(getKeyForType(type), !isEnabled).apply()
    }

    private fun isBiometricDisabled(type: BiometricType): Boolean {
        return sharedPreferences.getBoolean(getKeyForType(type), false)
    }

    private fun getKeyForType(type: BiometricType): String {
        return when (type) {
            LOGIN -> PREF_KEY_LOGIN_DISABLED
            TRANSFER -> PREF_KEY_TRANSFER_DISABLED
        }
    }
}
