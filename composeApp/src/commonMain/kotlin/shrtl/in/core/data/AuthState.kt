package shrtl.`in`.core.data

import AuthResult

sealed interface AuthState {
    data object Unauthenticated : AuthState

    data object Authenticating : AuthState

    data class Authenticated(val auth: AuthResult) : AuthState

    data object AuthError : AuthState
}
