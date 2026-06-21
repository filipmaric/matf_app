package com.example.matfapp.auth

import com.example.matfapp.auth.AuthSession

sealed class AuthState {
    data object Loading : AuthState()
    data object LoggedOut : AuthState()
    data class LoggedIn(val session: AuthSession) : AuthState()
    data class Error(val message: String) : AuthState()
}
