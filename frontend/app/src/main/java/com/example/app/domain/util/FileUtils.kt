package com.example.app.domain.util

import android.content.Context
import android.net.Uri
import java.io.File

fun uriToFile(
    context: Context,
    uri: Uri
): File {

    val inputStream = context.contentResolver.openInputStream(uri)!!
    val file = File.createTempFile("upload", ".jpg")

    file.outputStream().use { output ->
        inputStream.copyTo(output)
    }

    return file
}