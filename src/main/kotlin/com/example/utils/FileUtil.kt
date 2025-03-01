package com.example.utils

import java.io.File
import java.util.Base64
import java.util.UUID

/**
 * Decodifica una cadena Base64 y guarda el contenido en un fichero.
 * Las imágenes se almacenan en una carpeta propia para cada usuario.
 * Retorna la ruta donde se ha guardado la imagen.
 *
 * Nota: Actualmente se guarda la imagen con extensión .png.
 * Si se requieren otros formatos, se debería detectar el tipo a partir de la cadena o de un campo adicional.
 */
fun saveBase64Image(base64: String, userId: Int, baseDirectory: String = "uploads/images"): String {
    // Define la carpeta específica para el usuario
    val userDirectory = "$baseDirectory/$userId"
    val dir = File(userDirectory)
    if (!dir.exists()) {
        dir.mkdirs()
    }
    // Genera un nombre de fichero único, por ejemplo usando UUID
    val fileName = "img_${UUID.randomUUID()}.png"
    val filePath = "$userDirectory/$fileName"
    // Decodifica la cadena Base64
    val imageBytes = Base64.getDecoder().decode(base64)
    // Escribe los bytes en el fichero
    File(filePath).writeBytes(imageBytes)
    return filePath
}

/**
 * Elimina el fichero especificado por su ruta.
 */
fun deleteImage(filePath: String): Boolean {
    val file = File(filePath)
    return file.exists() && file.delete()
}

/**
 * Elimina el fichero especificado por su ruta y, si el directorio contenedor queda vacío,
 * también elimina el directorio.
 */
fun deleteImageAndCleanDirectory(filePath: String): Boolean {
    val file = File(filePath)
    if (file.exists() && file.delete()) {
        // Obtenemos el directorio padre del fichero
        val parentDir = file.parentFile
        // Si el directorio existe y está vacío, lo eliminamos
        if (parentDir != null && parentDir.isDirectory && parentDir.listFiles()?.isEmpty() == true) {
            parentDir.delete()
        }
        return true
    }
    return false
}
