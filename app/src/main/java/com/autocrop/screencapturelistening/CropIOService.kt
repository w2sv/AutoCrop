package com.autocrop.screencapturelistening

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import com.autocrop.activities.iodetermination.SavingResult
import com.autocrop.activities.iodetermination.processCropBundle
import com.autocrop.dataclasses.CropBundle
import com.autocrop.dataclasses.Screenshot
import com.autocrop.preferences.UriPreferences
import com.autocrop.screencapturelistening.notification.NotificationGroup
import com.autocrop.screencapturelistening.notification.NotificationId
import com.autocrop.screencapturelistening.serviceextensions.UnboundService
import com.autocrop.utils.android.IMAGE_MIME_TYPE
import com.autocrop.utils.android.extensions.getParcelable
import com.autocrop.utils.android.extensions.notificationBuilderWithSetChannel
import com.autocrop.utils.android.extensions.openBitmap
import com.autocrop.utils.android.extensions.showNotification
import com.lyrebirdstudio.croppylib.CropEdges
import timber.log.Timber

class CropIOService: UnboundService() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.i("CropIOService.onStartCommand; startId: $startId")
        PendingIntentRequestCode.cropIOService.remove(startId)

        with(intent!!){
            startService(setClass(this@CropIOService, NotificationCancellationService::class.java))
            carryOutIOAndShowNotification(
                data!!,
                getParcelable(ScreenCaptureListeningService.CROP_EDGES_EXTRA_KEY)
            )
        }

        stopSelf()
        return START_REDELIVER_INTENT
    }
    private val notificationGroup = NotificationGroup(
        this,
        NotificationId.DETECTED_NEW_CROPPABLE_SCREENSHOT
    )

    private fun carryOutIOAndShowNotification(screenshotUri: Uri, cropEdges: CropEdges){
        saveCrop(screenshotUri, cropEdges).let { (successfullySaved, writeUri) ->
            if (successfullySaved)
                showNotification(
                    notificationGroup.children.newId(),  // TODO
                    notificationBuilderWithSetChannel(
                        notificationGroup.channelId,
                        "Saved crop",
                        "Tap to view"
                    )
                        .setAutoCancel(true)
                        .setContentIntent(
                            PendingIntent.getActivity(
                                this,
                                PendingIntentRequestCode.viewCrop.addNewId(), // TODO: remove afterwards
                                Intent(Intent.ACTION_VIEW)
                                    .setDataAndType(
                                        writeUri,
                                        IMAGE_MIME_TYPE
                                    )
                                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION),
                                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
                            )
                        )
                )
        }
    }

    private fun saveCrop(screenshotUri: Uri, cropEdges: CropEdges): SavingResult {
        val (savingResult, _) = processCropBundle(
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