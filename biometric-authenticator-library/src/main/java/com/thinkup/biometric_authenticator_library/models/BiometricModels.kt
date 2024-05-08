package com.thinkup.biometric_authenticator_library.models

/**
 * Created by Martin Zarzar on 1/4/24.
 */
enum class BiometricAuthStatus(val id: Int) {
    READY_NEEDS_SETUP(2),
    READY(1),
    NOT_AVAILABLE(-1),
    TEMPORARILY_NOT_AVAILABLE(-2),
    AVAILABLE_BUT_NOT_ENROLLED(-3)
}

enum class BiometricType() {
    LOGIN,
    TRANSFER
}
