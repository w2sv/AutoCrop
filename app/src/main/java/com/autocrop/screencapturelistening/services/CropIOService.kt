package com.autocrop.screencapturelistening.services

import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.provider.MediaStore
import android.text.SpannableStringBuilder
import androidx.core.app.NotificationCompat
import androidx.core.text.italic
import com.autocrop.Screenshot
import com.autocrop.activities.iodetermination.IOResult
import com.autocrop.activities.iodetermination.carryOutCropIO
import com.autocrop.activities.iodetermination.pathTail
import com.autocrop.preferences.UriPreferences
import com.autocrop.screencapturelistening.abstractservices.BoundService
import com.autocrop.screencapturelistening.notifications.NotificationGroup
import com.autocrop.screencapturelistening.notifications.NotificationId
import com.autocrop.utils.android.IMAGE_MIME_TYPE
import com.autocrop.utils.android.extensions.getParcelable
import com.autocrop.utils.android.extensions.queryMediaStoreDatum
import com.w2sv.autocrop.R

class CropIOService :
    BoundService(),
    OnPendingIntentService.ClientInterface by OnPendingIntentService.Client(1) {

    companion object {
        private const val EXTRA_WRAPPED_INTENT = "com.autocrop.WRAPPED_INTENT"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        with(intent!!) {
            startService(
                setClass(this@CropIOService, OnPendingIntentService::class.java)
            )

            val ioResult = carryOutCropIO(
                crop = BitmapFactory.decodeFile(getStringExtra(ScreenshotListener.EXTRA_CROP_FILE_PATH)),
                screenshotMediaStoreData = getParcelable(ScreenshotListener.EXTRA_SCREENSHOT_MEDIASTORE_DATA)!!,
                deleteScreenshot = getBooleanExtra(ScreenshotListener.EXTRA_ATTEMPT_SCREENSHOT_DELETION, false)
            )
            if (getBooleanExtra(DeleteRequestActivity.EXTRA_CONFIRMED_DELETION, false))
                ioResult.deletedScreenshot = true

            showNotification(ioResult)
        }
        return START_REDELIVER_INTENT
    }

    private fun carryOutCropIO(crop: Bitmap, screenshotMediaStoreData: Screenshot.MediaStoreData, deleteScreenshot: Boolean): IOResult =
        contentResolver.carryOutCropIO(
            crop,
            screenshotMediaStoreData,
            UriPreferences.validDocumentUri(this),
            deleteScreenshot
        )

    private fun showNotification(ioResult: IOResult) {
        if (ioResult.successfullySavedCrop) {
            val notificationId = notificationGroup.children.newId()
            val associatedRequestCodes = requestCodes.makeAndAddMultiple(3)

            fun actionPendingIntent(requestCodeIndex: Int, wrappedIntent: Intent): PendingIntent =
                PendingIntent.getService(
                    this,
                    associatedRequestCodes[requestCodeIndex],
                    Intent(this, OnPendingIntentService::class.java)
                        .putOnPendingIntentServiceClientExtras(notificationId, associatedRequestCodes, true)
                        .putExtra(
                            EXTRA_WRAPPED_INTENT,
                            wrappedIntent
                                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        ),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                )

            notificationGroup.addChild(
                notificationId,
                notificationGroup.childBuilder(
                    buildString {
                        append("Saved crop")
                        if (ioResult.deletedScreenshot == true)
                            append(" & deleted screenshot")
                    }
                )
                    .setStyle(
                        NotificationCompat.BigTextStyle()
                            .bigText(
                                SpannableStringBuilder()
                                    .append("Saved to ")
                                    .italic {
                                        append(
                                            pathTail(
                                                contentResolver.queryMediaStoreDatum(
                                                    ioResult.writeUri!!,
                                                    MediaStore.Images.Media.DATA
                                                )
                                            )
                                        )
                                    }
                            )
                    )
                    .addAction(
                        NotificationCompat.Action(
                            R.drawable.ic_search_24,
                            "View",
                            actionPendingIntent(
                                0,
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
                                .putOnPendingIntentServiceClientExtras(notificationId, associatedRequestCodes),
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
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

    override fun onPendingIntentService(intent: Intent) {
        intent.getParcelable<Intent>(EXTRA_WRAPPED_INTENT)?.let {
            startActivity(
                it
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }
}