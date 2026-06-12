package com.example.app.domain.util

import android.content.Context
import android.net.Uri
import java.io.File
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlin.math.max
fun uriToFile(context: Context, uri: Uri): File {
    val inputStream = context.contentResolver.openInputStream(uri)!!
    val file = File.createTempFile("upload", ".jpg")

    file.outputStream().use { output ->
        inputStream.copyTo(output)
    }
    return file
}
fun uriToCompressedFile(context: Context, uri: Uri): File {
    val inputStream =
        context.contentResolver
            .openInputStream(uri)!!

    val bitmap =
        BitmapFactory.decodeStream(inputStream)

    val maxSize = 1280

    val width = bitmap.width
    val height = bitmap.height

    val scale =
        maxSize.toFloat() /
                max(width, height)

    val newWidth =
        (width * scale).toInt()

    val newHeight =
        (height * scale).toInt()

    val scaledBitmap =
        if (
            width > maxSize ||
            height > maxSize
        ) {
            Bitmap.createScaledBitmap(
                bitmap,
                newWidth,
                newHeight,
                true
            )
        } else {
            bitmap
        }

    val file =
        File.createTempFile(
            "upload",
            ".jpg"
        )

    file.outputStream().use { output ->

        scaledBitmap.compress(
            Bitmap.CompressFormat.JPEG,
            85,
            output
        )
        bitmap.recycle()

        if (scaledBitmap != bitmap) {
            scaledBitmap.recycle()
        }
    }

    return file
}