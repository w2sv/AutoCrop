package com.w2sv.domain.model

import android.net.Uri

sealed interface ScreenshotDeletionResult {
    data object SuccessfullyDeleted : ScreenshotDeletionResult
    data object DeletionFailed : ScreenshotDeletionResult

    @JvmInline
    value class DeletionApprovalRequired(val requestUri: Uri) : ScreenshotDeletionResult
}
