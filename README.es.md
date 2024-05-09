# AOS-BiometricAuthenticator
BiometricAuthenticator es una librería que facilita la autenticación biométrica en aplicaciones Android usando Jetpack Compose.

## Características
- Autenticación con huella digital y reconocimiento facial.
- Fácil integración con Jetpack Compose.
- Personalización del diálogo de autenticación.

### Instalación
Para incluir `BiometricAuthenticator` en tu proyecto, añade la siguiente dependencia a tu archivo `build.gradle`:
```groovy
dependencies {
    implementation 'com.thinkup:biometric-authenticator-library:1.0'
}
```

### Uso Básico
Para utilizar `BiometricAuthenticator`, primero asegúrate de que tu Activity extienda de `FragmentActivity`.

### Ejemplo de Uso
Aquí hay un ejemplo básico de cómo utilizar `BiometricAuthDialog` para autenticar a un usuario:
```kotlin
import com.thinkup.biometric_authenticator_library.components.BiometricAuthDialog
```

```kotlin
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
            BiometricAuthDialog(
                context = context,
                prefKey = "KEY_FOR_LOGIN",
                title = "Activar biometría",
                subtitle = "¿Quieres permitir el uso de biometría para acceder?",
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
```

### Funciones Disponibles

#### BiometricAuthDialog
`BiometricAuthDialog` permite abrir un diálogo de autenticación biométrica.

**Parámetros**:
- `context`: El contexto actual.
- `prefKey`: Clave para guardar preferencias.
- `title`: Título del diálogo.
- `subtitle`: Subtítulo del diálogo.
- `negativeButtonText`: Texto del botón negativo.
- `onSuccess`: Lambda ejecutada en autenticación exitosa.
- `onFailed`: Lambda ejecutada cuando la autenticación falla.
- `onError`: Lambda ejecutada cuando ocurre un error en la autenticación, con parámetros `errorCode` (código del error) y `errString` (mensaje de error).
- `onCanceled`: Lambda ejecutada si el usuario cancela la autenticación.

## Funciones Disponibles

### checkBiometricAvailability

Verifica si el dispositivo soporta y está configurado para autenticación biométrica.

**Parámetros**:
- `prefKey`: Clave utilizada para almacenar el token cifrado.
- `type`: Tipo de autenticación biométrica (`LOGIN` o `TRANSFER`).

**Retorno**:
- `BiometricAuthStatus`: Estado de la disponibilidad de la autenticación biométrica.

**Ejemplo**:
```kotlin
val biometricStatus = biometricAuthenticator.checkBiometricAvailability("KEY_FOR_LOGIN", BiometricType.LOGIN)
if (biometricStatus == BiometricAuthStatus.READY) {
    // Proceder con la autenticación
} else {
    // Manejar otros estados
}
```
### encryptToken

Cifra un token utilizando autenticación biométrica para mayor seguridad.

**Parámetros**:
- `token`: La cadena de texto que se desea cifrar.
- `authResult`: Resultado de la autenticación biométrica que contiene el `Cipher`.
- `prefKeyToken`: Clave para almacenar el token cifrado en `SharedPreferences`.

**Ejemplo**:
```kotlin
biometricAuthenticator.encryptToken("mySensitiveToken", authResult, "KEY_FOR_LOGIN")
```

### decryptToken

Desencripta un token cifrado usando autenticación biométrica.

**Parámetros**:
- `authResult`: Resultado de la autenticación biométrica que contiene el `Cipher`.
- `prefKey`: Clave para recuperar el token cifrado de `SharedPreferences`.

**Retorno**:
- `String`: Token descifrado.

**Ejemplo**:
```kotlin
val decryptedToken = biometricAuthenticator.decryptToken(authResult, "KEY_FOR_LOGIN")
```
