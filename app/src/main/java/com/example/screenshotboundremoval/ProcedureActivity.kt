package com.example.screenshotboundremoval

import android.app.AlertDialog
import android.app.Dialog
import android.content.ContentResolver
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
import java.io.InputStream


class ProcedureDialog(private val savedImageUri: Uri) : AppCompatDialogFragment(){

    private class KeepButtonOnClickListener: DialogInterface.OnClickListener{
        override fun onClick(dialog: DialogInterface?, which: Int) {
        }
    }

    private class DiscardButtonOnClickListener(val savedImageUri: Uri): DialogInterface.OnClickListener{
        override fun onClick(dialog: DialogInterface?, which: Int) {
            deleteImage()
        }

        private fun deleteImage(){
            val deleteFile = File(savedImageUri.path!!)

            if (!deleteFile.exists())
                println("file doesn't exist")
            else
                println("file exists")

            deleteFile.delete()
            println("deleted cropped image")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(this.activity)
        builder
            .setTitle("How do you want to proceed?")
            .setMessage("")
            .setPositiveButton("Keep Image", KeepButtonOnClickListener())
            .setNegativeButton("Discard", DiscardButtonOnClickListener(savedImageUri))

        return builder.create()
    }
}


class ProcedureActivity : AppCompatActivity() {
    private fun openDialog(savedImageUri: Uri){
        val dialog = ProcedureDialog(savedImageUri)
        dialog.show(supportFragmentManager, "procedure")
    }

    private fun restartMainActivity(){
        startActivity(Intent(this, MainActivity::class.java))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val savedImageUri: Uri = intent.getParcelableExtra(SAVED_IMAGE_URI)!!

        // reload image, to be refactored
        val imageStream: InputStream? = contentResolver.openInputStream(savedImageUri)
        val reloadedImage: Bitmap? = BitmapFactory.decodeStream(imageStream)

        // display cropped image
        setContentView(R.layout.display_screen)  // crashing when procedure activity layout seized
        image_view.setImageBitmap(reloadedImage)

        // query procedure
        openDialog(savedImageUri)

        // go back to main activity
        restartMainActivity()
    }
}
