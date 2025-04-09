package com.example.drawingapp.ui

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.drawingapp.R
import com.example.drawingapp.model.DrawingPath
import com.example.drawingapp.utils.BitmapUtils

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var drawingView: DrawingView

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
            val bitmap = drawingView.getBitmap()
            val uri = BitmapUtils.saveBitmapToGallery(this, bitmap)
            Toast.makeText(this, "Saved to gallery: $uri", Toast.LENGTH_SHORT).show()
        }

        findViewById<ImageButton>(R.id.ib_brush).setOnClickListener {
            showBrushSizeChooserDialog()
        }

        findViewById<ImageButton>(R.id.ib_gallery).setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 101)
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

                Log.d("ColorChange", "Changing color to: $color")
                drawingView.setColor(color)

            } catch (e: Exception) {
                Log.e("ColorChange", "Invalid color tag: $tagValue", e)
            }
        }
    }

    fun showBrushSizeChooserDialog() {
        Log.e("BrushDialog", "Opening brush dialog")

        val brushDialog = Dialog(this)
        brushDialog.setContentView(R.layout.dialog_brush_size)
        brushDialog.setTitle("Brush size:")

        val smallBtn = brushDialog.findViewById<ImageButton>(R.id.ib_small_brush)
        val mediumBtn = brushDialog.findViewById<ImageButton>(R.id.ib_medium_brush)
        val largeBtn = brushDialog.findViewById<ImageButton>(R.id.ib_large_brush)

        smallBtn.setOnClickListener {
            drawingView.setStrokeWidth(5f)
            brushDialog.dismiss()
        }

        mediumBtn.setOnClickListener {
            drawingView.setStrokeWidth(10f)
            brushDialog.dismiss()
        }

        largeBtn.setOnClickListener {
            drawingView.setStrokeWidth(20f)
            brushDialog.dismiss()
        }

        brushDialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 101 && resultCode == Activity.RESULT_OK) {
            val imageUri: Uri? = data?.data
            findViewById<android.widget.ImageView>(R.id.iv_background)?.setImageURI(imageUri)
        }
    }
}
