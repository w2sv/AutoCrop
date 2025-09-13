package com.w2sv.cropping.io

import android.content.ContentResolver
import android.net.Uri
import android.provider.MediaStore
import com.w2sv.common.util.log
import com.w2sv.cropping.io.extensions.queryMediaStoreData
import com.w2sv.domain.model.ImageMimeType
import com.w2sv.domain.model.Screenshot.MediaStoreData
import slimber.log.i

fun queryMediaStoreData(contentResolver: ContentResolver, uri: Uri): MediaStoreData {
    i { "queryMediaStoreData for: $uri" } // content://media/picker/0/com.android.providers.media.photopicker/media/1000016069
    return contentResolver.queryMediaStoreData(
        uri = uri,
        columns = arrayOf(
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.MIME_TYPE
            //                        MediaStore.Images.Media._ID  TODO: leads to java.lang.IllegalArgumentException: Unexpected picker URI projection. Uri:content://com.android.providers.media.photopicker/media/1000000346. Column:_id
        ),
        onCursor = {
            val fileName = it.getString(it.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME))
            MediaStoreData(
                diskUsage = it.getLong(it.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)),
                fileName = fileName,
                mimeType = ImageMimeType.parse(it.getString(it.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE))),
                id = uri.lastPathSegment?.toLongOrNull() ?: fileName.substringBeforeLast(".").toLong() // TODO
                //                            it.getLongOrNull(it.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
            )
                .log()
        }
    )!!
}
