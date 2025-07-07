package com.example.drawingapp.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log

object BitmapUtils {

    fun saveBitmapToGallery(context: Context, bitmap: Bitmap): android.net.Uri? {
        val filename = "Drawing_${System.currentTimeMillis()}.png"
        val resolver = context.contentResolver
        var uri: android.net.Uri? = null

        try {
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/DrawingApp")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }

            uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

            if (uri != null) {
                resolver.openOutputStream(uri)?.use { outputStream ->
                    val saved = bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    Log.d("BitmapUtils", "Image saved: $saved, URI: $uri")
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    resolver.update(uri, contentValues, null, null)
                }
            } else {
                Log.e("BitmapUtils", "Failed to create new MediaStore record.")
            }

        } catch (e: Exception) {
            Log.e("BitmapUtils", "Error saving bitmap", e)
            uri = null
        }

        return uri
    }
}
