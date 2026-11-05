package com.devcore.ticketflow.console.utils

import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object Logger {
    private val logFile = File("errores.txt")

    fun logError(operacion: String, descripcion: String) {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        val logMessage = "[$timestamp] Error en $operacion: $descripcion\n"
        try {
            val writer = FileWriter(logFile, true) // Modo append
            writer.append(logMessage)
            writer.close()
        } catch (e: IOException) {
            println("No se pudo escribir en el archivo de log: ${e.message}")
        }
    }
}
