package com.w2sv.autocrop.utils

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import slimber.log.i

@RequiresApi(Build.VERSION_CODES.Q)
fun getMediaUri(context: Context, uri: Uri): Uri? =
    try {
        if (DocumentsContract.isDocumentUri(context, uri))
            MediaStore.getMediaUri(context, uri)!!
                .also {
                    i { "Converted to mediaUri: $it" }
                }
        else
            uri
    }
    catch (e: IllegalArgumentException) {
        null
    }
