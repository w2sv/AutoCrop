package com.example.screenshotboundremoval

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.appcompat.app.AppCompatDialogFragment
import kotlinx.android.synthetic.main.display_screen.*
import java.io.InputStream
import java.time.Instant
import java.time.format.DateTimeFormatter


class ProcedureDialog : AppCompatDialogFragment(){
    private class SaveButtonOnClickListener: DialogInterface.OnClickListener{
        override fun onClick(dialog: DialogInterface?, which: Int) {
        }
    }

    private class DiscardButtonOnClickListener: DialogInterface.OnClickListener{
        override fun onClick(dialog: DialogInterface?, which: Int) {
            TODO("not implemented")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(this.activity)
        builder
            .setTitle("How do you want to proceed?")
            .setMessage("")
            .setPositiveButton("Save", SaveButtonOnClickListener())
            .setNegativeButton("Discard", DiscardButtonOnClickListener())

        return builder.create()
    }
}


class ProcedureActivity : AppCompatActivity() {
    private fun openDialog(){
        val dialog = ProcedureDialog()
        dialog.show(supportFragmentManager, "procedure")
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

        openDialog()
    }
}
