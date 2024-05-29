import com.russhwolf.settings.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object Storage {
    private const val KEY_SESSION = "sessionToken"
    private const val KEY_REFRESH = "refreshToken"
    private val settingsStorage = Settings()
    private val cache = mutableMapOf<String, String>()

    suspend fun saveTokensToStorage(
        sessionToken: String,
        refreshToken: String,
    ) {
        withContext(Dispatchers.Default) {
            cache[KEY_SESSION] = sessionToken
            cache[KEY_REFRESH] = refreshToken
            settingsStorage.putString(KEY_SESSION, sessionToken)
            settingsStorage.putString(KEY_REFRESH, refreshToken)
        }
    }

    suspend fun loadTokensFromStorage() =
        withContext(Dispatchers.Default) {
            cache.getOrPut(KEY_SESSION) { settingsStorage.getString(KEY_SESSION, "") } to
                cache.getOrPut(KEY_REFRESH) { settingsStorage.getString(KEY_REFRESH, "") }
        }

    suspend fun clearData() {
        withContext(Dispatchers.Default) {
            settingsStorage.remove(KEY_SESSION)
            settingsStorage.remove(KEY_REFRESH)
        }
    }
}
