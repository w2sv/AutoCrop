package com.autocrop.screencapturelistening

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import com.autocrop.activities.iodetermination.SavingResult
import com.autocrop.activities.iodetermination.processCropBundle
import com.autocrop.dataclasses.CropBundle
import com.autocrop.dataclasses.Screenshot
import com.autocrop.preferences.UriPreferences
import com.autocrop.screencapturelistening.abstractservices.BoundService
import com.autocrop.screencapturelistening.notification.ASSOCIATED_NOTIFICATION_ID
import com.autocrop.screencapturelistening.notification.ASSOCIATED_PENDING_REQUEST_CODES
import com.autocrop.screencapturelistening.notification.NotificationGroup
import com.autocrop.screencapturelistening.notification.NotificationId
import com.autocrop.screencapturelistening.notification.ScopeWideUniqueIds
import com.autocrop.utils.android.IMAGE_MIME_TYPE
import com.autocrop.utils.android.extensions.getParcelable
import com.autocrop.utils.android.extensions.openBitmap
import com.lyrebirdstudio.croppylib.CropEdges
import com.w2sv.autocrop.R
import timber.log.Timber

class CropIOService : BoundService() {
    companion object{
        const val WRAPPED_INTENT = "WRAPPED_INTENT_KEY"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        with(intent!!) {
            startService(setClass(this@CropIOService, NotificationCancellationService::class.java))
            carryOutIOAndShowNotification(
                screenshotUri = data!!,
                cropEdges = getParcelable(ScreenCaptureListeningService.CROP_EDGES_EXTRA_KEY)
            )
        }
        return START_REDELIVER_INTENT
    }

    val notificationGroup = NotificationGroup(
        this,
        NotificationId.SUCCESSFULLY_SAVED_CROP,
        { "Saved $it crops" }
    )
    val pendingRequestCodes = ScopeWideUniqueIds()

    private fun carryOutIOAndShowNotification(screenshotUri: Uri, cropEdges: CropEdges) {
        saveCrop(screenshotUri, cropEdges).let { (successfullySaved, writeUri) ->
            if (successfullySaved){
                val dynamicChildId = notificationGroup.children.newId()
                Timber.i("New id: $dynamicChildId")
                val viewRequestCode = pendingRequestCodes.addNewId()
                val shareRequestCode = pendingRequestCodes.addNewId()

                notificationGroup.addAndShowChild(
                    dynamicChildId,
                    notificationGroup.childBuilder("Saved crop to $writeUri")
                        .addAction(
                            NotificationCompat.Action(
                                R.drawable.ic_search_24,
                                "View",
                                actionPendingIntent(
                                    viewRequestCode,
                                    shareRequestCode,
                                    dynamicChildId,
                                    Intent(Intent.ACTION_VIEW)
                                        .setDataAndType(
                                            writeUri,
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
                                    shareRequestCode,
                                    viewRequestCode,
                                    dynamicChildId,
                                    Intent.createChooser(
                                        Intent(Intent.ACTION_SEND)
                                            .setType(IMAGE_MIME_TYPE)
                                            .putExtra(Intent.EXTRA_STREAM, writeUri),
                                        null
                                    )
                                )
                            )
                        )
                )
            }
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

    private fun actionPendingIntent(requestCode: Int, associatedRequestCode: Int, notificationId: Int, wrappedIntent: Intent): PendingIntent =
        PendingIntent.getService(
            this,
            requestCode,
            Intent(this, CropIOServiceActionIntentInterceptor::class.java)
                .putExtra(
                    WRAPPED_INTENT,
                    wrappedIntent
                        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                )
                .putExtra(ASSOCIATED_NOTIFICATION_ID, notificationId)
                .putExtra(ASSOCIATED_PENDING_REQUEST_CODES, intArrayOf(requestCode, associatedRequestCode)),
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

    override fun onDestroy() {
        super.onDestroy()

        stopService(Intent(this, CropIOServiceActionIntentInterceptor::class.java))
    }
}