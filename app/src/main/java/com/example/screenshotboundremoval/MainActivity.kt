package com.example.screenshotboundremoval

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import kotlinx.android.synthetic.main.activity_main.*
import java.time.Instant
import java.time.format.DateTimeFormatter


//TODO: progress bar screen, edge case handling, welcome screen, selection screen pimp, robustness elaboration
//      dir cropping, Logo

const val OLD_URIS: String = "com.example.screenshotboundremoval.OLD_URIS"
const val CROPPED_URIS: String = "com.example.screenshotboundremoval.CROPPED_URIS"

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

        // procedure result display
        val resultCode: Int = intent.getIntExtra(DELETION_RESULT, -1)

        when(resultCode){
            ProcedureDialog.DELETED_ORIGINAL_IMAGE -> displayMessage("white", "darkgray", "Deleted original screenshot")
        }

        setContentView(R.layout.activity_main)

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
        println("multiple image selection")
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    // ----------------
    // SAVING
    // ----------------

    private fun getFileName(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor: Cursor? = contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            } finally {
                cursor?.close()
            }
        }
        if (result.isNullOrEmpty()) {
            result = uri.path
            val cut = result!!.lastIndexOf('/')
            if (cut != -1)
                result = result.substring(cut + 1)
        }
        return result
    }

    private fun saveImage(croppedBitmap: Bitmap, originalTitle: String?): Uri {
        fun String.replaceMultiple(toBeReplaced: List<String>, replaceWith: String): String = this.run { var copy = this; toBeReplaced.forEach { copy = copy.replace(it, replaceWith) }; copy }

        fun modifyOriginalTitle(): String = "Cropped" + originalTitle!!.substring(0, originalTitle.indexOf(".")) + ".jpg"
        fun getNewTitle(): String = "CroppedScreenshot" + DateTimeFormatter.ISO_INSTANT.format(
            Instant.now()).toString().replaceMultiple(listOf(":", ".", "_", "T"), "-").removeSuffix("Z") + ".jpg"

        val title: String = if(!originalTitle.isNullOrEmpty()) modifyOriginalTitle() else getNewTitle()

        return MediaStore.Images.Media.insertImage(
            contentResolver,
            croppedBitmap,
            title,
        "").toUri()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE){

            val oldUris: ArrayList<Uri> = arrayListOf() // old image uri -> respectively cropped image uri
            val croppedUris: ArrayList<Uri> = arrayListOf()
            for (i in 0 until data?.clipData?.itemCount!!) {
                // retrieve uri and resolve into bitmap
                val imageUri: Uri? = data.clipData?.getItemAt(i)?.uri
                val image: Bitmap? = BitmapFactory.decodeStream(contentResolver.openInputStream(imageUri!!))

                // crop image
                val time = System.currentTimeMillis()  // !
                val croppedImage: Bitmap = Cropper(image).getCroppedBitmap()
                val croppingDuration = System.currentTimeMillis() - time
                println("cropping took $croppingDuration ms")

                // save image
                val originalTitle: String? = getFileName(imageUri)
                val savedImageUri: Uri = saveImage(croppedImage, originalTitle)

                oldUris.add(imageUri)
                croppedUris.add(savedImageUri)
            }
            // start procedure activity
            startProcedureActivity(oldUris, croppedUris)
        }
    }

    // --------------
    // PROCEDURE ACTIVITY
    // --------------

    private fun startProcedureActivity(oldUris: ArrayList<Uri>, croppedUris: ArrayList<Uri>){
        val intent: Intent = Intent(this, ProcedureActivity::class.java)
            .apply{putExtra(OLD_URIS, oldUris)}
            .apply{putExtra(CROPPED_URIS, croppedUris)}
        startActivity(intent)
    }
}
