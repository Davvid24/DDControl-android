import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow

class SessionManager(context: Context) {

    private val prefs = context.getSharedPreferences("ddcontrol_prefs", Context.MODE_PRIVATE)

    companion object {
        const val KEY_TOKEN      = "token"
        const val KEY_USER_ID    = "userId"
        const val KEY_EMPRESA_ID = "empresaId"
        const val KEY_NOMBRE     = "nombre"
        const val KEY_ROL        = "rol"

        val isLoggedInFlow = MutableStateFlow(false)
    }

    fun saveSession(token: String, userId: Int, empresaId: Int, nombre: String, rol: String) {
        prefs.edit()
            .putString(KEY_TOKEN,      token)
            .putInt(KEY_USER_ID,       userId)
            .putInt(KEY_EMPRESA_ID,    empresaId)
            .putString(KEY_NOMBRE,     nombre)
            .putString(KEY_ROL,        rol)
            .apply()
        isLoggedInFlow.value = true
    }

    fun clearSession() {
        prefs.edit().clear().apply()
        isLoggedInFlow.value = false
    }

    fun init() {
        isLoggedInFlow.value = getToken() != null
    }

    fun getToken():     String? = prefs.getString(KEY_TOKEN, null)
    fun getUserId():    Int     = prefs.getInt(KEY_USER_ID, -1)
    fun getEmpresaId(): Int     = prefs.getInt(KEY_EMPRESA_ID, -1)
    fun getNombre():    String? = prefs.getString(KEY_NOMBRE, null)
    fun getRol():       String? = prefs.getString(KEY_ROL, null)
    fun isLoggedIn():   Boolean = getToken() != null
}