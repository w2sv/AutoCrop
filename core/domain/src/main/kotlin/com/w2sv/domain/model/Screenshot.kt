package com.w2sv.domain.model

import android.content.ContentResolver
import android.graphics.Bitmap
import android.net.Uri
import android.os.Parcelable
import com.w2sv.androidutils.graphics.loadBitmap
import kotlinx.parcelize.Parcelize

@Parcelize
data class Screenshot(val uri: Uri, val height: Int, val mediaStoreData: MediaStoreData) : Parcelable {

    @Parcelize
    data class MediaStoreData(val diskUsage: Long, val fileName: String, val mimeType: ImageMimeType, val id: Long) : Parcelable

    fun loadBitmap(contentResolver: ContentResolver): Bitmap =
        contentResolver.loadBitmap(uri)!!

    sealed interface DeletionResult {
        data object SuccessfullyDeleted : DeletionResult
        data object DeletionFailed : DeletionResult

        @JvmInline
        value class ApprovalRequired(val requestUri: Uri) : DeletionResult
    }
}
