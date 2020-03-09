package com.example.screenshotboundremoval

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDialogFragment
import kotlinx.android.synthetic.main.display_screen.*
import java.io.File


const val DELETION_RESULT: String = "com.example.screenshotboundremoval.DELETION_RESULT"


class ProcedureDialog(val originalImageUri: Uri, val savedImageUri: Uri, val activityContext: Context) : AppCompatDialogFragment(){

    companion object{
        const val DELETED_ORIGINAL_IMAGE = 1337
    }

    private fun getRealPathFromURI(context: Context, contentUri: Uri): String? {
        var cursor: Cursor? = null
        return try {
            val images: Array<String> = arrayOf(MediaStore.Images.Media.DATA)
            cursor = context.contentResolver.query(contentUri, images, null, null, null)

            val columnIndex = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            cursor.getString(columnIndex)
        } catch (e: Exception) {
            println( "getRealPathFromURI Exception : $e")
            ""
        } finally {
            cursor?.close()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(this.activity)
        builder
            .setTitle("How do you want to proceed?")
            .setPositiveButton("Discard", DiscardButtonOnClickListener())
            .setNegativeButton("Keep", KeepButtonOnClickListener())
            .setNeutralButton("Keep and delete original screenshot", OriginalImageDeletionOnClickListener())

        return builder.create()
    }

    private fun restartMainActivity(resultCode: Int?){
        val intent = Intent(context, MainActivity::class.java).apply{
            this.putExtra(DELETION_RESULT, resultCode)
        }
        startActivity(intent)
    }

    private fun deleteImage(uri: Uri){
        val deleteFile = File(getRealPathFromURI(activityContext, uri)!!)

        if (deleteFile.exists()){
            // deleteFile.canonicalFile.delete()
            deleteFile.canonicalFile.deleteRecursively()

            if(deleteFile.exists())
                activityContext.deleteFile(deleteFile.name)

            activityContext.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(File(getRealPathFromURI(activityContext, uri)!!))))
        }
    }

    // ---------------
    // BUTTON CLASSES
    // ---------------

    private inner class KeepButtonOnClickListener: DialogInterface.OnClickListener{
        override fun onClick(dialog: DialogInterface?, which: Int) = restartMainActivity(-1)
    }

    private inner class DiscardButtonOnClickListener: DialogInterface.OnClickListener{
        override fun onClick(dialog: DialogInterface?, which: Int) {
            deleteImage(savedImageUri)
            restartMainActivity(-1)
        }
    }

    private inner class OriginalImageDeletionOnClickListener: DialogInterface.OnClickListener{
        override fun onClick(dialog: DialogInterface?, which: Int) {
            deleteImage(originalImageUri)
            restartMainActivity(DELETED_ORIGINAL_IMAGE)
        }
    }
}


class ProcedureActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val originalImageUri: Uri = intent.getParcelableExtra(ORIGINAL_IMAGE_URI)!!
        val savedImageUri: Uri = intent.getParcelableExtra(SAVED_IMAGE_URI)!!

        // reload image
        val reloadedImage: Bitmap? = BitmapFactory.decodeStream(contentResolver.openInputStream(savedImageUri))

        // display cropped image
        setContentView(R.layout.display_screen)  // crashing when procedure activity layout seized
        image_view.setImageBitmap(reloadedImage)

        // query procedure
        openDialog(originalImageUri, savedImageUri)
    }
    private fun openDialog(originalImageUri: Uri, savedImageUri: Uri){
        val dialog = ProcedureDialog(originalImageUri, savedImageUri, this)
        dialog.show(supportFragmentManager, "procedure")
    }
}
