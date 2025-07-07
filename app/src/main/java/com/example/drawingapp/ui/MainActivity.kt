package com.example.drawingapp.ui

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.example.drawingapp.R
import com.example.drawingapp.model.DrawingPath
import com.example.drawingapp.utils.BitmapUtils

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var drawingView: DrawingView

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                saveDrawingToGallery()
            } else {
                Toast.makeText(this, "Storage permission denied.", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawingView = findViewById(R.id.drawing_view)

        setupButtons()
        observePaths()
    }

    private fun setupButtons() {
        findViewById<ImageButton>(R.id.ib_undo).setOnClickListener {
            viewModel.undo()
        }

        findViewById<ImageButton>(R.id.ib_redo).setOnClickListener {
            viewModel.redo()
        }

        findViewById<ImageButton>(R.id.ib_save).setOnClickListener {
            handleSaveButtonClick()
        }

        findViewById<ImageButton>(R.id.ib_brush).setOnClickListener {
            showBrushSizeChooserDialog()
        }

        findViewById<ImageButton>(R.id.ib_gallery).setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply {
                type = "image/*"
            }
            galleryPickerLauncher.launch(intent)
        }
    }

    private val galleryPickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageUri: Uri? = result.data?.data
                findViewById<ImageView>(R.id.iv_background)?.setImageURI(imageUri)
            }
        }

    private fun observePaths() {
        viewModel.paths.observe(this) { paths ->
            drawingView.updatePaths(paths)
        }
    }

    fun onPathDrawn(path: DrawingPath) {
        viewModel.addPath(path)
    }

    fun paintClicked(view: View) {
        if (view is ImageButton) {
            val tagValue = view.tag?.toString() ?: return

            try {
                val color = if (tagValue.startsWith("#")) {
                    Color.parseColor(tagValue)
                } else {
                    val colorResId = resources.getIdentifier(tagValue, "color", packageName)
                    if (colorResId != 0) {
                        ContextCompat.getColor(this, colorResId)
                    } else {
                        throw IllegalArgumentException("Invalid color resource: $tagValue")
                    }
                }

                drawingView.setColor(color)

            } catch (e: Exception) {
                Log.e("ColorChange", "Invalid color tag: $tagValue", e)
            }
        }
    }

    fun showBrushSizeChooserDialog() {
        val brushDialog = Dialog(this).apply {
            setContentView(R.layout.dialog_brush_size)
            setTitle("Brush size:")
        }

        brushDialog.findViewById<ImageButton>(R.id.ib_small_brush).setOnClickListener {
            drawingView.setStrokeWidth(5f)
            brushDialog.dismiss()
        }
        brushDialog.findViewById<ImageButton>(R.id.ib_medium_brush).setOnClickListener {
            drawingView.setStrokeWidth(10f)
            brushDialog.dismiss()
        }
        brushDialog.findViewById<ImageButton>(R.id.ib_large_brush).setOnClickListener {
            drawingView.setStrokeWidth(20f)
            brushDialog.dismiss()
        }

        brushDialog.show()
    }

    private fun handleSaveButtonClick() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                // No runtime permission required for saving images on Android 10+
                saveDrawingToGallery()
            }
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED -> {
                saveDrawingToGallery()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }

    private fun saveDrawingToGallery() {
        val bitmap = drawingView.getBitmap()
        val uri = BitmapUtils.saveBitmapToGallery(this, bitmap)
        if (uri != null) {
            Toast.makeText(this, "Saved to gallery successfully!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Failed to save image.", Toast.LENGTH_SHORT).show()
        }
    }
}
