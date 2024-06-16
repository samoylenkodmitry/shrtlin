package shrtl.`in`.ui.screen

import UrlInfo

sealed interface Screen {
    data object Splash : Screen

    data object Main : Screen

    data object UserProfile : Screen

    data object Login : Screen

    data class Error(
        val message: String,
    ) : Screen

    data class Card(
        val info: UrlInfo,
    ) : Screen

    data class QrCode(
        val info: UrlInfo,
    ) : Screen
}
