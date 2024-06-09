package shrtl.`in`.core

import Period
import UrlStats
import UrlsResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.withContext

object ViewModel {
    suspend fun openUrl(url: String) {
        withContext(Dispatchers.Default) {
            Navigator.openUrl(url)
        }
    }

    suspend fun shorten(s: String) =
        withContext(Dispatchers.Default) {
            try {
                Api.shorten(s)
            } catch (e: Exception) {
                e.message ?: "Error $e"
                null
            }
        }

    suspend fun getUrls(
        page: Int,
        pageSize: Int,
    ): UrlsResponse =
        withContext(Dispatchers.Default) {
            try {
                Api.getUrls(page, pageSize)
            } catch (e: Exception) {
                e.message ?: "Error $e"
                UrlsResponse(emptyList(), 0)
            }
        }

    suspend fun removeUrl(urlId: Long): Boolean =
        withContext(Dispatchers.Default) {
            try {
                Api.removeUrl(urlId)
            } catch (e: Exception) {
                e.message ?: "Error $e"
                false // Return false on error
            }
        }

    suspend fun logout() {
        withContext(Dispatchers.Default + SupervisorJob()) {
            try {
                Api.logout()
            } catch (e: Exception) {
                e.message ?: "Error $e"
            }
        }
    }

    suspend fun checkAuth() {
        withContext(Dispatchers.Default + SupervisorJob()) {
            try {
                Api.checkAuth()
            } catch (e: Exception) {
                e.message ?: "Error $e"
                UrlsResponse(emptyList(), 0)
            }
        }
    }

    suspend fun doLogin(userId: String): Boolean =
        withContext(Dispatchers.Default + SupervisorJob()) {
            try {
                Api.doLogin(userId)
            } catch (e: Exception) {
                e.message ?: "Error $e"
                false
            }
        }

    suspend fun updateNick(newNick: String): Boolean =
        withContext(Dispatchers.Default + SupervisorJob()) {
            try {
                Api.updateNick(newNick)
            } catch (e: Exception) {
                e.message ?: "Error $e"
                false
            }
        }

    suspend fun getClicks(
        urlId: Long,
        period: Period,
    ): UrlStats? =
        withContext(Dispatchers.Default) {
            try {
                Api.getClicks(urlId, period)
            } catch (e: Exception) {
                e.message ?: "Error $e"
                null
            }
        }
}
