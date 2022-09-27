package com.autocrop.activities.examination

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import com.autocrop.utils.android.extensions.queryMediaColumn
import timber.log.Timber

fun Context.imageDeletionInquiryUri(uri: Uri): Uri? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
        uri.mediaUriWithAppendedId(this)
            .also { Timber.i("Returned mediaUriWithAppendedId: $it") }
    else
        null

@RequiresApi(Build.VERSION_CODES.Q)
private fun Uri.mediaUriWithAppendedId(context: Context): Uri =
    ContentUris.withAppendedId(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        context.mediaUriId(this)
    )

@RequiresApi(Build.VERSION_CODES.Q)
private fun Context.mediaUriId(uri: Uri): Long =
    contentResolver
        .queryMediaColumn(uri, MediaStore.Images.Media._ID)
        .toLong()