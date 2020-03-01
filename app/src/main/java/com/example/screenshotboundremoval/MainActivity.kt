package com.example.screenshotboundremoval

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.io.InputStream

import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {
    companion object{
        private const val IMAGE_PICK_CODE = 1000
        private const val READ_PERMISSION_CODE = 1001
        private const val WRITE_PERMISSION_CODE = 1002
    }

    /*
    * invoked when activity created; resets previous states, initializes screen, requests permissions
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        image_selection_button.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                println("permission required")
                val requiredPermissions: Array<String> = arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                    .filter { checkSelfPermission(it) == PackageManager.PERMISSION_DENIED }
                    .toTypedArray()

                for (el in requiredPermissions) {
                    when (el) {
                        "Manifest.permission.READ_EXTERNAL_STORAGE" -> requestPermissions(
                            requiredPermissions,
                            READ_PERMISSION_CODE
                        )
                        "Manifest.permission.WRITE_EXTERNAL_STORAGE" -> requestPermissions(
                            requiredPermissions,
                            WRITE_PERMISSION_CODE
                        ).also { print("requesting write permission") }
                    }
                }
            }
            else
                println("no permission required")
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            READ_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Read permission denied", Toast.LENGTH_SHORT).show()
                }
            }
            WRITE_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Write permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // pickImageFromGallery()
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }



    //handle result of picked image
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE){
            val imageUri: Uri? = data?.data
            // val path: String? = imageUri?.path
            val imageStream: InputStream? = contentResolver.openInputStream(imageUri!!)
            val bitmap: Bitmap? = BitmapFactory.decodeStream(imageStream)

            val cropper = Cropper(bitmap)
            val croppedBitmap: Bitmap = cropper.getCroppedBitmap()

            image_view.setImageBitmap(bitmap)

            val path: File = Environment.getExternalStorageDirectory()
            val newFile = File(path, "cropped.png")
            val fileOut = FileOutputStream(newFile)

            println("Foo")

            croppedBitmap.compress(Bitmap.CompressFormat.PNG, 20, fileOut)
            fileOut.flush()
            fileOut.close()

            println("Foo1")
            MediaStore.Images.Media.insertImage(contentResolver, croppedBitmap, newFile.absolutePath, newFile.absolutePath)

            // val pixels: IntArray = IntArray(bitmap!!.width * bitmap.height)


            // bitmap.getPixels(pixels, 0 ,1,0,0, bitmap.width, bitmap.height)

            // println(pixels)

            // val pixels = IntArray(bitmap.width * bitmap.height)
            // image_view.setImageURI(data.data)
        }
    }
}
