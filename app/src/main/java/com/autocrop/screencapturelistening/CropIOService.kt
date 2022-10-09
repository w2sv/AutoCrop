package com.autocrop.screencapturelistening

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import com.autocrop.activities.iodetermination.SavingResult
import com.autocrop.activities.iodetermination.processCropBundle
import com.autocrop.dataclasses.CropBundle
import com.autocrop.dataclasses.Screenshot
import com.autocrop.preferences.UriPreferences
import com.autocrop.utils.android.IMAGE_MIME_TYPE
import com.autocrop.utils.android.extensions.getParcelable
import com.autocrop.utils.android.extensions.notificationBuilderWithSetChannel
import com.autocrop.utils.android.extensions.notificationManager
import com.autocrop.utils.android.extensions.openBitmap
import com.autocrop.utils.android.extensions.showNotification
import com.lyrebirdstudio.croppylib.CropEdges

class CropIOService: Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent!!.process()
        stopSelf()
        return START_REDELIVER_INTENT
    }

    private fun Intent.process(){
        val notificationId = getIntExtra(ScreenCaptureListeningService.CLOSE_NOTIFICATION_ID_EXTRA_KEY, -1)
        notificationManager().cancel(notificationId)
        startService(
            Intent(this@CropIOService, OnPostNotificationCancellationService::class.java)
                .putExtra(
                    ScreenCaptureListeningService.CLOSE_NOTIFICATION_ID_EXTRA_KEY,
                    notificationId
                )
        )

//        saveCrop(
//            getParcelable(ScreenCaptureListeningService.SCREENSHOT_URI_EXTRA_KEY),
//            getParcelable(ScreenCaptureListeningService.CROP_EDGES_EXTRA_KEY)
//        ).let { (successfullySaved, writeUri) ->
//            if (successfullySaved)
//                showNotification(
//                    NotificationId.SUCCESSFULLY_SAVED_CROP,
//                    notificationBuilderWithSetChannel(
//                        NotificationId.SUCCESSFULLY_SAVED_CROP,
//                        "Saved crop",
//                        "Tap to view"
//                    )
//                        .setAutoCancel(true)
//                        .setContentIntent(
//                            PendingIntent.getActivity(
//                                this@CropIOService,
//                                PendingIntentRequestCode.VIEW_CROP.ordinal,
//                                Intent(Intent.ACTION_VIEW)
//                                    .setDataAndType(
//                                        writeUri,
//                                        IMAGE_MIME_TYPE
//                                    )
//                                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION),
//                                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
//                            )
//                        )
//                )
//            else
//                showNotification(
//                    NotificationId.NO_CROP_EDGES_FOUND,
//                    "No crop edges found",
//                    "No crop edges found",
//                    "Tap to crop manually"
//                )
//        }
    }

    private fun saveCrop(screenshotUri: Uri, cropEdges: CropEdges): SavingResult {
        val (savingResult, successfullyDeleted) = processCropBundle(
            CropBundle.assemble(
                Screenshot(
                    screenshotUri,
                    -1,
                    listOf(),
                    Screenshot.MediaStoreColumns.query(
                        contentResolver,
                        screenshotUri
                    )
                ),
                contentResolver.openBitmap(screenshotUri),
                cropEdges
            ),
            UriPreferences.documentUri,
            false
        )
        return savingResult
    }
}