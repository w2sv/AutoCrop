package com.example.screenshotboundremoval

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment

class DialogOnClickListener: DialogInterface.OnClickListener{
    override fun onClick(dialog: DialogInterface?, which: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

class ProcedureDialog : AppCompatDialogFragment(){
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(this.activity)
        builder
            .setTitle("Title")
            .setMessage("How do you want to proceed?")
            .setPositiveButton("save", DialogOnClickListener())

        return builder.create()
    }
}