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
import kotlinx.android.synthetic.main.display_screen.*
import java.io.InputStream

import java.io.File
import java.io.FileOutputStream

//TODO: algorithm speed up, loading screen, exception handling, welcome screen, selection screen pimp, saving

class MainActivity : AppCompatActivity() {
    companion object{
        private const val IMAGE_PICK_CODE = 1000
        private const val READ_PERMISSION_CODE = 1001
        private const val WRITE_PERMISSION_CODE = 1002
    }

    private val permission2Code: Map<String, Int> = mapOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE to WRITE_PERMISSION_CODE,
        Manifest.permission.READ_EXTERNAL_STORAGE to READ_PERMISSION_CODE)
    var requiredPermissions: Int = 0

    private fun checkPermission(permission: String){
        if (checkSelfPermission(permission) == PackageManager.PERMISSION_DENIED){
            requestPermissions(arrayOf(permission), permission2Code[permission]!!)
            requiredPermissions ++
        }
    }

    private fun requestActivityPermissions(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    /*
     * invoked when activity created; resets previous states, initializes screen, requests permissions
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        image_selection_button.setOnClickListener {
            requestActivityPermissions()

            if (requiredPermissions == 0)
                pickImageFromGallery()
        }
    }

    private fun permissionRequestResultHandling(grantResults: IntArray, requestDescription: String){
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_DENIED)
            Toast.makeText(this, "$requestDescription permission denied", Toast.LENGTH_SHORT).show()
        else
            requiredPermissions --
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            READ_PERMISSION_CODE -> permissionRequestResultHandling(grantResults, "Read")
            WRITE_PERMISSION_CODE -> permissionRequestResultHandling(grantResults, "Write")
        }
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
            val imageStream: InputStream? = contentResolver.openInputStream(imageUri!!)
            val bitmap: Bitmap? = BitmapFactory.decodeStream(imageStream)

            // cropping image
            val cropper = Cropper(bitmap)
            val croppedBitmap: Bitmap = cropper.getCroppedBitmap()

            setContentView(R.layout.display_screen)
            image_view.setImageBitmap(croppedBitmap)

            // saving new image
//            val path: File = Environment.getExternalStorageDirectory()
//            val newFile = File(path ,"cropped.png")
//
//            println("post creation")
//
//            val fileOut = FileOutputStream(newFile)
//
//            println("Foo")
//
//            croppedBitmap.compress(Bitmap.CompressFormat.PNG, 20, fileOut)
//            fileOut.flush()
//            fileOut.close()
//
//            println("Foo1")
//            MediaStore.Images.Media.insertImage(contentResolver, croppedBitmap, newFile.absolutePath, newFile.absolutePath)

        }
    }
}
