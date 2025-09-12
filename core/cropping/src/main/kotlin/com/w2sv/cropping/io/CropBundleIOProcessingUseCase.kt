package com.w2sv.cropping.io

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import com.w2sv.common.di.AppIoScope
import com.w2sv.common.util.log
import com.w2sv.cropping.io.extensions.deleteImage
import com.w2sv.domain.model.CropBundle
import com.w2sv.domain.model.CropBundleProcessingResult
import com.w2sv.domain.model.Screenshot
import com.w2sv.domain.repository.PreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import javax.inject.Inject

class CropBundleIOProcessingUseCase @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    @AppIoScope scope: CoroutineScope
) {
    private val deleteScreenshots = preferencesRepository.deleteScreenshots.stateIn(scope, SharingStarted.Eagerly)

    fun invoke(
        cropBundle: CropBundle,
        context: Context,
        deleteScreenshot: Boolean = deleteScreenshots.value
    ): CropBundleProcessingResult =
        invoke(
            cropBitmap = cropBundle.crop.bitmap,
            screenshotMediaStoreData = cropBundle.screenshot.mediaStoreData,
            context = context,
            deleteScreenshot = deleteScreenshot
        )

    fun invoke(
        cropBitmap: Bitmap,
        screenshotMediaStoreData: Screenshot.MediaStoreData,
        context: Context,
        deleteScreenshot: Boolean = deleteScreenshots.value
    ): CropBundleProcessingResult =
        CropBundleProcessingResult(
            cropFileUri = context.contentResolver.saveBitmap(
                bitmap = cropBitmap,
                mimeType = screenshotMediaStoreData.mimeType,
                fileName = cropFileName(
                    fileName = screenshotMediaStoreData.fileName,
                    mimeType = screenshotMediaStoreData.mimeType
                ),
                parentDocumentUri = preferencesRepository.getWritableCropSaveDirDocumentUriOrNull(context)
            ),
            screenshotDeletionResult = if (deleteScreenshot) {
                screenshotDeletionResult(
                    mediaStoreId = screenshotMediaStoreData.id,
                    contentResolver = context.contentResolver
                )
            }
            else {
                null
            }
        )
}

private fun screenshotDeletionResult(mediaStoreId: Long, contentResolver: ContentResolver): Screenshot.DeletionResult =
    when (IMAGE_DELETION_REQUIRING_APPROVAL) {
        true ->
            Screenshot.DeletionResult.ApprovalRequired(
                getImageContentUri(mediaStoreId)
            )

        false -> when (contentResolver.deleteImage(mediaStoreId)) {
            true -> Screenshot.DeletionResult.SuccessfullyDeleted
            false -> Screenshot.DeletionResult.DeletionFailed
        }
    }
        .log { "ScreenshotDeletionResult: $it" }
