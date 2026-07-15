package com.mindforce.mindlog.data.remote

object NetworkUtils {
    /** Le backend renvoie les erreurs métier au format {"error": "..."} (voir GlobalExceptionHandler) */
    fun extractErrorMessage(raw: String?, fallback: String = "Une erreur est survenue"): String {
        if (raw.isNullOrBlank()) return fallback
        return try {
            val regex = Regex("\"error\"\\s*:\\s*\"([^\"]*)\"")
            regex.find(raw)?.groupValues?.get(1) ?: fallback
        } catch (e: Exception) {
            fallback
        }
    }
}
