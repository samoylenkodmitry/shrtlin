package shrtl.`in`.core

import UrlInfo
import getUrlOpener
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import shrtl.`in`.ui.screen.Screen

object Navigator {
    val screenFlow = MutableSharedFlow<Screen>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    fun openUrl(url: String) {
        getUrlOpener().openUrl(url)
    }

    fun main() {
        screenFlow.tryEmit(Screen.Main)
    }

    fun userProfile() {
        screenFlow.tryEmit(Screen.UserProfile)
    }

    fun error(message: String) {
        screenFlow.tryEmit(Screen.Error(message))
    }

    fun splash() {
        screenFlow.tryEmit(Screen.Splash)
    }

    fun login() {
        screenFlow.tryEmit(Screen.Login)
    }

    fun card(info: UrlInfo) {
        screenFlow.tryEmit(Screen.Card(info))
    }

    fun qrCode(info: UrlInfo) {
        screenFlow.tryEmit(Screen.QrCode(info))
    }
}
