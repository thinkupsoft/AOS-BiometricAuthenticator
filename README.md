
# AOS-BiometricAuthenticator
[![es](https://img.shields.io/badge/lang-es-yellow.svg)](https://github.com/thinkupsoft/AOS-BiometricAuthenticator/blob/main/README.es.md)

BiometricAuthenticator is a library that facilitates biometric authentication in Android applications using Jetpack Compose.

## Features
- Fingerprint and facial recognition authentication.
- Easy integration with Jetpack Compose.
- Customizable authentication dialog.

### Installation
To include `BiometricAuthenticator` in your project, add the following dependency to your `build.gradle` file:
```groovy
dependencies {
    implementation 'com.thinkup:biometric-authenticator-library:1.1'
}
```

### Basic Usage
To use `BiometricAuthenticator`, first ensure that your Activity extends `FragmentActivity`.

### Usage Example
Here is a basic example of how to use `BiometricAuthDialog` to authenticate a user:
```kotlin
import com.thinkup.biometric_authenticator_library.components.BiometricAuthDialog

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
            title = "Enable biometrics",
            subtitle = "Do you want to allow the use of biometrics to access?",
            negativeButtonText = "Cancel",
            onSuccess = {
                showDialog = false
                feedbackColor = Color.Green
            },
            onFailed = {
                showDialog = false
                feedbackColor = Color.Red
            },
            onError = { errorCode, errString ->
                showDialog = false
                feedbackColor = Color.Red
            },
            onCanceled = {
                showDialog = false
                feedbackColor = Color.Red
            }
        )
    }
}
```

## Available Functions

### BiometricAuthDialog

`BiometricAuthDialog` allows opening a biometric authentication dialog.

**Parameters**:
- `context`: The current context.
- `prefKey`: Key to store preferences.
- `title`: Dialog title.
- `subtitle`: Dialog subtitle.
- `negativeButtonText`: Text of the negative button.
- `onSuccess`: Lambda executed on successful authentication.
- `onFailed`: Lambda executed when authentication fails.
- `onError`: Lambda executed when an error occurs in the authentication, with parameters `errorCode` (error code) and `errString` (error message).
- `onCanceled`: Lambda executed if the user cancels the authentication.

### checkBiometricAvailability

Checks if the device supports and is set up for biometric authentication.

**Parameters**:
- `prefKey`: Key used to store the encrypted token.
- `type`: Type of biometric authentication (`LOGIN` or `TRANSFER`).

**Return**:
- `BiometricAuthStatus`: Status of the availability of biometric authentication.

**Example**:
```kotlin
val biometricStatus = biometricAuthenticator.checkBiometricAvailability("KEY_FOR_LOGIN", BiometricType.LOGIN)
if (biometricStatus == BiometricAuthStatus.READY) {
    // Proceed with authentication
} else {
    // Handle other states
}
```

### encryptToken

Encrypts a token using biometric authentication for added security.

**Parameters**:
- `token`: The string to be encrypted.
- `authResult`: Result of the biometric authentication that contains the `Cipher`.
- `prefKeyToken`: Key to store the encrypted token in `SharedPreferences`.

**Example**:
```kotlin
biometricAuthenticator.encryptToken("mySensitiveToken", authResult, "KEY_FOR_LOGIN")
```

### decryptToken

Decrypts a token encrypted using biometric authentication.

**Parameters**:
- `authResult`: Result of the biometric authentication that contains the `Cipher`.
- `prefKey`: Key to retrieve the encrypted token from `SharedPreferences`.

**Return**:
- `String`: Decrypted token.

**Example**:
```kotlin
val decryptedToken = biometricAuthenticator.decryptToken(authResult, "KEY_FOR_LOGIN")
```
