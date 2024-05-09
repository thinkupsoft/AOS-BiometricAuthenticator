package com.thinkup.biometric_authenticator_library.components

import android.content.Context
import androidx.biometric.BiometricPrompt.AuthenticationResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.fragment.app.FragmentActivity
import com.thinkup.biometric_authenticator_library.BiometricAuthenticator

/**
 * Created by Martin Zarzar on 17/4/24.
 */
@Composable
fun BiometricAuthDialog(
    context: Context,
    prefKey: String,
    title: String,
    subtitle: String,
    resetKey: Boolean = false,
    negativeButtonText: String,
    onSuccess: (AuthenticationResult) -> Unit,
    onFailed: () -> Unit = {},
    onError: (errorCode: Int, errString: String) -> Unit = { _, _ -> },
    onCanceled: () -> Unit = {}
) {
    val activity = context as FragmentActivity
    val biometricAuthenticator = BiometricAuthenticator(context)

    DisposableEffect(Unit) {
        biometricAuthenticator.promptBiometricAuthentication(
            prefKey = prefKey,
            title = title,
            subtitle = subtitle,
            negativeButtonText = negativeButtonText,
            activity = activity,
            resetKey = resetKey,
            onSuccess = onSuccess,
            onFailed = onFailed,
            onError = onError,
            onCanceled = onCanceled
        )

        onDispose { }
    }
}