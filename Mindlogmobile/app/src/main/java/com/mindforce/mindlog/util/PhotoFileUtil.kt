package com.mindforce.mindlog.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object PhotoFileUtil {

    private const val AUTHORITY_SUFFIX = ".fileprovider"

    /** Crée un fichier vide dans le cache de l'app, destiné à recevoir la photo prise par la caméra */
    fun createCameraOutputFile(context: Context): File {
        val dir = File(context.cacheDir, "panne_photos").apply { mkdirs() }
        return File(dir, "panne_${UUID.randomUUID()}.jpg")
    }

    /** URI content:// exposée via FileProvider, à donner à l'intent de capture caméra */
    fun uriForFile(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(context, context.packageName + AUTHORITY_SUFFIX, file)
    }

    /**
     * Copie le contenu d'une image choisie dans la galerie (content://...) vers un fichier local,
     * pour pouvoir l'envoyer ensuite en multipart/form-data.
     */
    fun copyContentUriToFile(context: Context, uri: Uri): File? {
        return try {
            val dir = File(context.cacheDir, "panne_photos").apply { mkdirs() }
            val extension = guessExtension(context, uri)
            val outputFile = File(dir, "panne_${UUID.randomUUID()}.$extension")

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(outputFile).use { output ->
                    input.copyTo(output)
                }
            }
            outputFile
        } catch (e: Exception) {
            null
        }
    }

    private fun guessExtension(context: Context, uri: Uri): String {
        val type = context.contentResolver.getType(uri) ?: return "jpg"
        return when {
            type.contains("png") -> "png"
            type.contains("webp") -> "webp"
            type.contains("heic") -> "heic"
            else -> "jpg"
        }
    }
}
