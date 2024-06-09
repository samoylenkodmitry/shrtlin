package shrtl.`in`.core

import AuthResult
import com.russhwolf.settings.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

object Storage {
    private const val KEY_AUTH = "key_auth"
    private val settingsStorage = Settings()
    private val objCache = mutableMapOf<String, Any?>()
    private val jsonInstance =
        Json {
            isLenient = true
            ignoreUnknownKeys = true
        }

    suspend fun clearData() {
        withContext(Dispatchers.Default) {
            objCache.clear()
            settingsStorage.remove(KEY_AUTH)
        }
    }

    suspend fun saveAuth(auth: AuthResult) {
        withContext(Dispatchers.Default) {
            objCache[KEY_AUTH] = auth
            settingsStorage.putString(KEY_AUTH, jsonInstance.encodeToString(AuthResult.serializer(), auth))
        }
    }

    suspend fun loadAuth(): AuthResult? =
        withContext(Dispatchers.Default) {
            objCache.getOrPut(KEY_AUTH) {
                settingsStorage.getString(KEY_AUTH, "").takeIf { it.isNotBlank() }?.let {
                    jsonInstance.decodeFromString(AuthResult.serializer(), it)
                }
            } as AuthResult?
        }
}
