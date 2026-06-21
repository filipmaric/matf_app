package rs.ac.bg.matf.auth

import rs.ac.bg.matf.auth.AuthSession

sealed class AuthState {
    data object Loading : AuthState()
    data object LoggedOut : AuthState()
    data class LoggedIn(val session: AuthSession) : AuthState()
    data class Error(val message: String) : AuthState()
}
