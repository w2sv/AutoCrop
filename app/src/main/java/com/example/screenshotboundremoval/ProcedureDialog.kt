package com.example.screenshotboundremoval

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatDialogFragment
import java.io.File
import java.io.FileOutputStream


class ProcedureDialog : AppCompatDialogFragment(){
    private class SaveButtonOnClickListener: DialogInterface.OnClickListener{
        /*private fun saveImage(){
            val path: File = Environment.getExternalStorageDirectory()
            val newFile = File(path ,"cropped.png")

            println("post creation")

            val fileOut = FileOutputStream(newFile)

            println("Foo")

            croppedBitmap.compress(Bitmap.CompressFormat.PNG, 20, fileOut)
            fileOut.flush()
            fileOut.close()

            println("Foo1")
            MediaStore.Images.Media.insertImage(contentResolver, croppedBitmap, newFile.absolutePath, newFile.absolutePath)
        }*/
        override fun onClick(dialog: DialogInterface?, which: Int) {
            // saveImage()
        }
    }

    private class DiscardButtonOnClickListener: DialogInterface.OnClickListener{
        override fun onClick(dialog: DialogInterface?, which: Int) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(this.activity)
        builder
            .setTitle("Title")
            .setMessage("How do you want to proceed?")
            .setPositiveButton("Save", SaveButtonOnClickListener())
            .setNegativeButton("Discard", DiscardButtonOnClickListener())

        return builder.create()
    }
}