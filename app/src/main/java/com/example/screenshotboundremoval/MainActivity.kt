package com.example.screenshotboundremoval

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


//TODO: progress bar screen, edge case handling, welcome screen, selection screen pimp, robustness elaboration
//      dir cropping, Logo

class MainActivity : AppCompatActivity() {
    companion object{
        private const val IMAGE_PICK_CODE = 69
        private const val READ_PERMISSION_CODE = 420
        private const val WRITE_PERMISSION_CODE = 47
    }

    private val permission2Code: Map<String, Int> = mapOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE to WRITE_PERMISSION_CODE,
        Manifest.permission.READ_EXTERNAL_STORAGE to READ_PERMISSION_CODE
    )

    private var nRequiredPermissions: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        // procedure result display if existent
        val resultCode: Int = intent.getIntExtra(DELETION_RESULT, -1)
        /*when(resultCode){
            ProcedureDialog.DELETED_ORIGINAL_IMAGE -> displayMessage("white", "darkgray", "Deleted original screenshot")
        }*/

        image_selection_button.setOnClickListener {
            requestActivityPermissions()

            if (nRequiredPermissions == 0)
                pickImageFromGallery()
        }
    }

    private fun displayMessage(textColor: String, backgroundColor: String, text: String){
        val toast = Toast.makeText(this, text, Toast.LENGTH_LONG)
        toast.view.setBackgroundColor(Color.parseColor(backgroundColor))

        val view = toast.view.findViewById<View>(android.R.id.message) as TextView
        view.setTextColor(Color.parseColor(textColor))

        toast.show()
    }

    // ----------------
    // PERMISSION QUERY
    // ----------------
    private fun checkPermission(permission: String){
        if (checkSelfPermission(permission) == PackageManager.PERMISSION_DENIED){
            requestPermissions(arrayOf(permission), permission2Code[permission]!!)
            nRequiredPermissions ++
        }
    }

    private fun requestActivityPermissions(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            READ_PERMISSION_CODE -> permissionRequestResultHandling(grantResults, "Read")
            WRITE_PERMISSION_CODE -> permissionRequestResultHandling(grantResults, "Write")
        }
    }

    private fun permissionRequestResultHandling(grantResults: IntArray, requestDescription: String){
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_DENIED)
            Toast.makeText(this, "$requestDescription permission denied", Toast.LENGTH_SHORT).show()
        else
            nRequiredPermissions --
    }

    // ----------------
    // IMAGE SELECTION
    // ----------------
    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    // ----------------
    // SAVING
    // ----------------
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE){

            for (i in 0 until data?.clipData?.itemCount!!) {
                // retrieve uri and resolve into bitmap
                val imageUri: Uri = data.clipData?.getItemAt(i)?.uri!!
                val image: Bitmap? = BitmapFactory.decodeStream(contentResolver.openInputStream(imageUri))

                // crop image
                val time = System.currentTimeMillis()  // !
                val croppedImage: Bitmap = Cropper(image!!).getCroppedImage()
                val croppingDuration = System.currentTimeMillis() - time
                println("CROPPING TOOK $croppingDuration MS")

                ImageCash.cash[imageUri] = croppedImage
            }
            startProcedureActivity()
        }
    }

    // --------------
    // PROCEDURE ACTIVITY
    // --------------
    private fun startProcedureActivity(){
        startActivity(Intent(this, ProcedureActivity::class.java))
    }
}
