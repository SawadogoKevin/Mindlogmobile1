package com.mindforce.mindlog.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "mindforce_session")

/**
 * Stocke localement la session du Chef de Département connecté :
 * token JWT, identité, et son département (renvoyés par /api/auth/verify).
 */
class SessionManager(private val context: Context) {

    private object Keys {
        val TOKEN = stringPreferencesKey("token")
        val EMAIL = stringPreferencesKey("email")
        val ROLE = stringPreferencesKey("role")
        val NOM = stringPreferencesKey("nom")
        val PRENOM = stringPreferencesKey("prenom")
        val USER_ID = longPreferencesKey("user_id")
        val DEPARTEMENT_ID = longPreferencesKey("departement_id")
        val DEPARTEMENT_NOM = stringPreferencesKey("departement_nom")
    }

    val tokenFlow: Flow<String?> = context.dataStore.data.map { it[Keys.TOKEN] }

    suspend fun saveSession(
        token: String,
        email: String,
        role: String,
        nom: String?,
        prenom: String?,
        userId: Long?,
        departementId: Long?,
        departementNom: String?
    ) {
        context.dataStore.edit { prefs ->
            prefs[Keys.TOKEN] = token
            prefs[Keys.EMAIL] = email
            prefs[Keys.ROLE] = role
            prefs[Keys.NOM] = nom ?: ""
            prefs[Keys.PRENOM] = prenom ?: ""
            userId?.let { prefs[Keys.USER_ID] = it }
            departementId?.let { prefs[Keys.DEPARTEMENT_ID] = it }
            prefs[Keys.DEPARTEMENT_NOM] = departementNom ?: ""
        }
    }

    suspend fun getToken(): String? = context.dataStore.data.first()[Keys.TOKEN]

    suspend fun getUserId(): Long? = context.dataStore.data.first()[Keys.USER_ID]

    suspend fun getDisplayName(): String {
        val prefs = context.dataStore.data.first()
        val prenom = prefs[Keys.PRENOM] ?: ""
        val nom = prefs[Keys.NOM] ?: ""
        return "$prenom $nom".trim()
    }

    suspend fun getDepartementNom(): String? = context.dataStore.data.first()[Keys.DEPARTEMENT_NOM]

    suspend fun getDepartementId(): Long? = context.dataStore.data.first()[Keys.DEPARTEMENT_ID]

    suspend fun saveDepartementId(id: Long) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DEPARTEMENT_ID] = id
        }
    }

    suspend fun saveDepartementNom(nom: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DEPARTEMENT_NOM] = nom
        }
    }

    suspend fun getEmail(): String? = context.dataStore.data.first()[Keys.EMAIL]
    suspend fun getRole(): String? = context.dataStore.data.first()[Keys.ROLE]
    suspend fun getNom(): String? = context.dataStore.data.first()[Keys.NOM]
    suspend fun getPrenom(): String? = context.dataStore.data.first()[Keys.PRENOM]

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}
