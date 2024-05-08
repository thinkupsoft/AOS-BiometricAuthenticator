package com.thinkup.biometricauthenticator

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.thinkup.biometric_authenticator_library.components.TouchIdDialog
import com.thinkup.biometricauthenticator.ui.theme.BiometricAuthenticatorTheme

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BiometricAuthenticatorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainContent()

                }
            }
        }
    }
}

@Composable
fun MainContent() {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    var feedbackColor by remember { mutableStateOf(Color.White) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(feedbackColor)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { showDialog = !showDialog }) {
            Text(text = "LOGIN")
        }

        if (showDialog) {
            TouchIdDialog(
                context = context,
                prefKey = "KEY_FOR_LOGIN",
                title = "Activar biometría",
                subtitle = "¿Quieres permitir el uso su huella digital o reconocimiento facial para acceder a tu cuenta?",
                negativeButtonText = "Cancelar",
                onSuccess = {
                    showDialog = false
                    feedbackColor = Color.Green
                },
                onCanceled = {
                    showDialog = false
                    feedbackColor = Color.Red
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BiometricAuthenticatorTheme {
        MainContent()
    }
}