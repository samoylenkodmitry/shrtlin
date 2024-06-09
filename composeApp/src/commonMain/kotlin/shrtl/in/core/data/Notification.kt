package shrtl.`in`.core.data

sealed class Notification(val duration: Int) {
    data class Error(val message: String) : Notification(duration = 5000)

    data class Info(val message: String) : Notification(duration = 3000)
}
