package com.w2sv.cropbundle.io

import android.content.ContentUris
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.ChecksSdkIntAtLeast

fun getImageContentUri(mediaStoreId: Long): Uri =
    ContentUris.withAppendedId(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        mediaStoreId
    )

@ChecksSdkIntAtLeast(api=Build.VERSION_CODES.R)
val IMAGE_DELETION_REQUIRING_APPROVAL: Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.R