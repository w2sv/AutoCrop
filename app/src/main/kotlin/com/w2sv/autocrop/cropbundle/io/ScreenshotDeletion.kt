package com.w2sv.autocrop.cropbundle.io

import android.content.ContentResolver
import android.content.ContentUris
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.w2sv.autocrop.cropbundle.io.extensions.deleteImage
import slimber.log.i

fun getDeleteRequestUri(mediaStoreId: Long): Uri? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
        ContentUris.withAppendedId(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            mediaStoreId
        )
            .also { i { "Built contentUriWithMediaStoreImagesId: $it" } }
    else
        null

fun ContentResolver.deleteScreenshotIfApplicable(mediaStoreId: Long, delete: Boolean): Boolean? =
    if (delete)
        deleteImage(mediaStoreId)
    else
        null