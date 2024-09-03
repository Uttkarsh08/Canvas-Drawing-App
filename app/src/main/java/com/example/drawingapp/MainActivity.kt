package com.example.drawingapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.MediaScannerConnection
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

//import yuku.ambilwarna.AmbilWarnaDialog


class MainActivity : AppCompatActivity() {

    private var drawingView: DrawingView? = null
    private var mImageButtonCurrentPaint : ImageButton? = null
//    private var mImageButtonColorPicker: ImageButton? = null

    var customProgressDialog: Dialog? = null

    val openGalleryLauncher : ActivityResultLauncher<Intent> = registerForActivityResult(     // launcher to select image from the gallery
        ActivityResultContracts.StartActivityForResult()){ result->
            if(result.resultCode == RESULT_OK && result.data != null){
                val imagebg: ImageView = findViewById(R.id.iv_background)
                imagebg.setImageURI(result.data?.data)
            }
    }

    private val requestPermission: ActivityResultLauncher<Array<String>> =    //launcher for permission request
        registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                val permissionName = it.key
                val isGranted = it.value

                if(isGranted){
                    Toast.makeText(this, "Permission Granted for Storage Images", Toast.LENGTH_SHORT).show()

                    val pickIntent  = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    openGalleryLauncher.launch(pickIntent)


                }else{
                    if(permissionName == Manifest.permission.READ_MEDIA_IMAGES){
                        Toast.makeText(this, "Permission Denied for Storage Images", Toast.LENGTH_SHORT).show()
                    }
                }
            }


        }

    //    private var mDefaultColor = 0

    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        drawingView = findViewById(R.id.drawing_view)
        drawingView?.setSizeForBrush(10.toFloat())

        val linearLayoutPaintColors = findViewById<LinearLayout>(R.id.ll_paint_colors)

        mImageButtonCurrentPaint = linearLayoutPaintColors[1] as ImageButton
        mImageButtonCurrentPaint!!.setImageDrawable(
            ContextCompat.getDrawable(this, R.drawable.pallet_pressed)
        )
//        mImageButtonColorPicker = linearLayoutPaintColors[7] as ImageButton

        val ib_gallery: ImageButton = findViewById(R.id.ib_gallery)
        ib_gallery.setOnClickListener {
            requestStoragePermission()
        }

        val ib_brush: ImageButton = findViewById(R.id.ib_brush)
        ib_brush.setOnClickListener {
            showBrushSizeDialog()
        }

        val ib_undo: ImageButton = findViewById(R.id.ib_undo)
        ib_undo.setOnClickListener {
            drawingView?.onclickUndo()
        }
        val ib_redo: ImageButton = findViewById(R.id.ib_redo)
        ib_redo.setOnClickListener {
            drawingView?.onclickRedo()
        }

        val ib_save: ImageButton = findViewById(R.id.ib_save)
        ib_save.setOnClickListener {
            if(isreadStorageAllowed()){
                showProgressDialog()
                lifecycleScope.launch {
                    val flDrawingView: FrameLayout = findViewById(R.id.fl_drawing_view_container)
                    saveBitmapFile(getBitmapFromView(flDrawingView))
                }
            }
        }

    }

    private fun showBrushSizeDialog(){
        val brushDialog = Dialog(this)
        brushDialog.setContentView(R.layout.dialog_brush_size)
        brushDialog.setTitle("Brush Size")

        val smallBtn: ImageButton = brushDialog.findViewById(R.id.ib_small_brush)
        smallBtn.setOnClickListener{
            drawingView?.setSizeForBrush(10.toFloat())
            brushDialog.dismiss()
        }

        val mediumBtn: ImageButton = brushDialog.findViewById(R.id.ib_medium_brush)
        mediumBtn.setOnClickListener{
            drawingView?.setSizeForBrush(20.toFloat())
            brushDialog.dismiss()
        }

        val largeBtn: ImageButton = brushDialog.findViewById(R.id.ib_large_brush)
        largeBtn.setOnClickListener{
            drawingView?.setSizeForBrush(30.toFloat())
            brushDialog.dismiss()
        }
        brushDialog.show()

    }

    fun paintClicked(view: View){
        if(view != mImageButtonCurrentPaint /*&& view != mImageButtonColorPicker*/){
            val imageButton = view as ImageButton
            val colorTag = imageButton.tag.toString()
            drawingView?.setColor(colorTag)

            imageButton.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.pallet_pressed)
            )

            mImageButtonCurrentPaint?.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.pallet_normal)
            )

            mImageButtonCurrentPaint = view
        }

    }

//    fun pickerClicked(view : View){
//        if(view == mImageButtonColorPicker){
//            openColorPickerDialogue()
//        }
//    }

//    fun openColorPickerDialogue(){
//        val colorPickerDialogue =  AmbilWarnaDialog(this, mDefaultColor,
//                object : AmbilWarnaDialog.OnAmbilWarnaListener {
//                    override fun onCancel(dialog: AmbilWarnaDialog?) {
//
//                    }
//
//                    override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
//                        mDefaultColor = color
//                        drawingView?.setColor(String.format("#%06X", 0xFFFFFF and color))
//
//                    }
//                })
//        colorPickerDialogue.show()
//        }


    private fun isreadStorageAllowed(): Boolean{
        val result = ContextCompat.checkSelfPermission(this,
            Manifest.permission.READ_MEDIA_IMAGES
            )
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun requestStoragePermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_MEDIA_IMAGES)){
            ShowRationaleDialog("Drawing App", "App needs access to External Storage")
        }else{
            requestPermission.launch(
                arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES
                )
            )
        }
    }



    private fun ShowRationaleDialog(
        title: String,
        message: String
    ){
        val builder : AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton("Cancel"){ dialog, _-> dialog.dismiss()}
        builder.create().show()

    }

    private fun getBitmapFromView(view: View): Bitmap{
        //Define a bitmap with the same size as the view
        val returnedBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        //Bind a canvas to it
        val canvas = Canvas(returnedBitmap)
        //Get the view's background
        val bgDrawable = view.background

        if(bgDrawable!=null){
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas)
        }else{
            //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE)
        }
        // draw the view on the canvas
        view.draw(canvas)
        //return the bitmap
        return returnedBitmap
    }

    private suspend fun saveBitmapFile(mbitmap: Bitmap?): String{
        var result = ""
        withContext(Dispatchers.IO){
            if(mbitmap!=null){
                try {
                    val bytes = ByteArrayOutputStream()
                    mbitmap.compress(Bitmap.CompressFormat.PNG, 90, bytes)

                    val f = File(externalCacheDir?.absoluteFile.toString()
                    +File.separator + "DrawingApp" + System.currentTimeMillis()/1000 +".jpg"
                    )

                    val fo = FileOutputStream(f)
                    fo.write(bytes.toByteArray())
                    fo.close()

                    result = f.absolutePath

                    runOnUiThread{
                        cancelProgressDialog()
                        if(result.isNotEmpty()){
                            Toast.makeText(this@MainActivity, "File Saved Successfully : $result", Toast.LENGTH_SHORT).show()
                            shareImage(result)
                        }else{
                            Toast.makeText(this@MainActivity, "Something Went Wrong While Saving the File", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                catch (e: Exception){
                    result = ""
                    e.printStackTrace()
                }
            }
        }
        return  result
    }
    private fun showProgressDialog() {
        customProgressDialog = Dialog(this@MainActivity)

        /*Set the screen content from a layout resource.
        The resource will be inflated, adding all top-level views to the screen.*/
        customProgressDialog?.setContentView(R.layout.dialog_custom_progress)

        //Start the dialog and display it on screen.
        customProgressDialog?.show()
    }

    private fun cancelProgressDialog() {
        if (customProgressDialog != null) {
            customProgressDialog?.dismiss()
            customProgressDialog = null
        }
    }

    private fun shareImage(result: String){
        MediaScannerConnection.scanFile(this, arrayOf(result), null){
            path, uri->
            val shareIntent = Intent()
            shareIntent.action =Intent.ACTION_SEND
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
            shareIntent.type = "image.png"
            startActivity(Intent.createChooser(shareIntent, "Share"))

        }

    }

}