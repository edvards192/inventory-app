package com.example.services

import java.io.File
import java.util.UUID

class FileStorageService {

    private val uploadDir = File("uploads")

    init {
        if (!uploadDir.exists()) {
            uploadDir.mkdirs()
        }
    }

    fun saveFile(
        bytes: ByteArray,
        originalFileName: String
    ): String {

        val extension =
            originalFileName.substringAfterLast(".", "")

        val uniqueFileName =
            "${UUID.randomUUID()}.$extension"

        val file =
            File(uploadDir, uniqueFileName)

        file.writeBytes(bytes)

        return "/uploads/$uniqueFileName"
    }
    fun deleteFile(url: String): Boolean {

        val fileName = url.substringAfterLast("/")

        val file = File(uploadDir, fileName)

        return if (file.exists()) {
            file.delete()
        } else {
            false
        }
    }
}