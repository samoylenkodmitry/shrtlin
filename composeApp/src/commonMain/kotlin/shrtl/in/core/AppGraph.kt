package shrtl.`in`.core

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import shrtl.`in`.core.data.AuthState
import shrtl.`in`.core.data.Notification

object AppGraph {
    val auth =
        MutableSharedFlow<AuthState>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST).apply {
            tryEmit(AuthState.Unauthenticated)
        }
    val notifications = MutableSharedFlow<Notification>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
}
