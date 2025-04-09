package com.example.drawingapp.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore


object BitmapUtils {
    fun saveBitmapToGallery(context: Context, bitmap: Bitmap): Uri? {
        val savedImageURL = MediaStore.Images.Media.insertImage(
            context.contentResolver,
            bitmap,
            "Drawing_" + System.currentTimeMillis(),
            "Drawing made in DrawingApp"
        )
        return savedImageURL?.let { Uri.parse(it) }
    }
}
