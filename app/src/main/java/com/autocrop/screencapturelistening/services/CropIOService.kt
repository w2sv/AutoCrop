package com.autocrop.screencapturelistening.services

import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.provider.MediaStore
import android.text.SpannableStringBuilder
import androidx.core.app.NotificationCompat
import androidx.core.text.bold
import com.autocrop.activities.iodetermination.IOResult
import com.autocrop.activities.iodetermination.carryOutCropIO
import com.autocrop.activities.iodetermination.pathTail
import com.autocrop.dataclasses.Screenshot
import com.autocrop.preferences.UriPreferences
import com.autocrop.screencapturelistening.abstractservices.BoundService
import com.autocrop.screencapturelistening.notifications.NotificationGroup
import com.autocrop.screencapturelistening.notifications.NotificationId
import com.autocrop.utils.android.IMAGE_MIME_TYPE
import com.autocrop.utils.android.extensions.getParcelable
import com.autocrop.utils.android.extensions.queryMediaStoreDatum
import com.autocrop.utils.kotlin.delegates.Consumable
import com.w2sv.autocrop.R

class CropIOService :
    BoundService(),
    OnPendingIntentService.ClientInterface by OnPendingIntentService.Client(1) {

    companion object {
        private const val EXTRA_WRAPPED_INTENT = "com.autocrop.WRAPPED_INTENT"

        var cropBitmap: Bitmap? by Consumable(null)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        with(intent!!) {
            startService(
                setClass(this@CropIOService, OnPendingIntentService::class.java)
            )

            val ioResult = carryOutCropIO(
                screenshotMediaStoreData = getParcelable(ScreenCaptureListeningService.EXTRA_SCREENSHOT_MEDIASTORE_DATA)!!,
                deleteScreenshot = getBooleanExtra(ScreenCaptureListeningService.EXTRA_ATTEMPT_SCREENSHOT_DELETION, false)
            )
            if (getBooleanExtra(DeleteRequestActivity.EXTRA_CONFIRMED_DELETION, false))
                ioResult.deletedScreenshot = true

            showNotification(ioResult)
        }
        return START_REDELIVER_INTENT
    }

    private fun carryOutCropIO(screenshotMediaStoreData: Screenshot.MediaStoreData, deleteScreenshot: Boolean): IOResult =
        contentResolver.carryOutCropIO(
            cropBitmap!!,
            screenshotMediaStoreData,
            UriPreferences.validDocumentUri(this),
            deleteScreenshot
        )

    private fun showNotification(ioResult: IOResult) {
        if (ioResult.successfullySavedCrop) {
            val notificationId = notificationGroup.children.newId()
            val associatedRequestCodes = requestCodes.makeAndAddMultiple(3)

            notificationGroup.addChild(
                notificationId,
                notificationGroup.childBuilder(
                    buildString {
                        append("Saved crop")
                        if (ioResult.deletedScreenshot == true)
                            append(" & deleted screenshot")
                    }
                )
                    .setContentText(
                        SpannableStringBuilder()
                            .bold { append("Path: ") }
                            .append(
                                pathTail(
                                    contentResolver.queryMediaStoreDatum(
                                        ioResult.writeUri!!,
                                        MediaStore.Images.Media.DATA
                                    )
                                )
                            )
                    )
                    .addAction(
                        NotificationCompat.Action(
                            R.drawable.ic_search_24,
                            "View",
                            actionPendingIntent(
                                0,
                                associatedRequestCodes,
                                notificationId,
                                Intent(Intent.ACTION_VIEW)
                                    .setDataAndType(
                                        ioResult.writeUri,
                                        IMAGE_MIME_TYPE
                                    )
                            )
                        )
                    )
                    .addAction(
                        NotificationCompat.Action(
                            R.drawable.ic_baseline_share_24,
                            "Share",
                            actionPendingIntent(
                                1,
                                associatedRequestCodes,
                                notificationId,
                                Intent.createChooser(
                                    Intent(Intent.ACTION_SEND)
                                        .setType(IMAGE_MIME_TYPE)
                                        .putExtra(Intent.EXTRA_STREAM, ioResult.writeUri),
                                    null
                                )
                            )
                        )
                    )
                    .setDeleteIntent(
                        PendingIntent.getService(
                            this,
                            associatedRequestCodes[2],
                            Intent(this, OnPendingIntentService::class.java)
                                .putClientExtras(notificationId, associatedRequestCodes),
                            PendingIntent.FLAG_UPDATE_CURRENT
                        )
                    )
            )
        }
    }

    override val notificationGroup = NotificationGroup(
        this,
        "IO Result",
        NotificationId.SAVED_CROP,
        { "Saved $it crops" }
    )

    private fun actionPendingIntent(
        requestCodeIndex: Int,
        associatedRequestCodes: ArrayList<Int>,
        notificationId: Int,
        wrappedIntent: Intent
    ): PendingIntent =
        PendingIntent.getService(
            this,
            associatedRequestCodes[requestCodeIndex],
            Intent(this, OnPendingIntentService::class.java)
                .putClientExtras(notificationId, associatedRequestCodes)
                .putExtra(
                    EXTRA_WRAPPED_INTENT,
                    wrappedIntent
                        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                )
                .putExtra(OnPendingIntentService.EXTRA_CANCEL_NOTIFICATION, true),
            PendingIntent.FLAG_UPDATE_CURRENT
        )

    override fun onPendingIntentService(intent: Intent) {
        intent.getParcelable<Intent>(EXTRA_WRAPPED_INTENT)?.let {
            startActivity(
                it
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        cropBitmap = null
    }
}