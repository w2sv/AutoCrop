package com.autocrop.screencapturelistening.services

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import com.autocrop.activities.iodetermination.SavingResult
import com.autocrop.activities.iodetermination.processCropBundle
import com.autocrop.dataclasses.CropBundle
import com.autocrop.dataclasses.Screenshot
import com.autocrop.preferences.UriPreferences
import com.autocrop.screencapturelistening.CANCEL_NOTIFICATION
import com.autocrop.screencapturelistening.notifications.NotificationGroup
import com.autocrop.screencapturelistening.notifications.NotificationId
import com.autocrop.screencapturelistening.services.abstractservices.BoundService
import com.autocrop.utils.android.IMAGE_MIME_TYPE
import com.autocrop.utils.android.extensions.getParcelable
import com.autocrop.utils.android.extensions.openBitmap
import com.lyrebirdstudio.croppylib.CropEdges
import com.w2sv.autocrop.R
import java.io.File

class CropIOService :
    BoundService(),
    OnPendingIntentService.ClientInterface by OnPendingIntentService.Client(1) {
    
    companion object{
        private const val WRAPPED_INTENT = "WRAPPED_INTENT_KEY"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        with(intent!!) {
            startService(setClass(this@CropIOService, OnPendingIntentService::class.java))
            carryOutIOAndShowNotification(
                screenshotUri = data!!,
                cropEdges = getParcelable(ScreenCaptureListeningService.CROP_EDGES_EXTRA_KEY)!!
            )
        }
        return START_REDELIVER_INTENT
    }

    override fun onCancellation(intent: Intent){
        intent.getParcelable<Intent>(WRAPPED_INTENT)?.let {
            startActivity(
                it
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }

    override val notificationGroup = NotificationGroup(
        this,
        NotificationId.SAVED_CROP,
        { "Saved $it crops" }
    )

    private fun carryOutIOAndShowNotification(screenshotUri: Uri, cropEdges: CropEdges) {
        saveCrop(screenshotUri, cropEdges).let { (successfullySaved, writeUri) ->
            if (successfullySaved){
                val notificationId = notificationGroup.children.newId()
                val associatedRequestCodes = requestCodes.makeAndAddMultiple(3)

                notificationGroup.addAndShowChild(
                    notificationId,
                    notificationGroup.childBuilder("Saved crop to ${File(writeUri.path!!).name}")
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
                                    1,
                                    associatedRequestCodes,
                                    notificationId,
                                    Intent.createChooser(
                                        Intent(Intent.ACTION_SEND)
                                            .setType(IMAGE_MIME_TYPE)
                                            .putExtra(Intent.EXTRA_STREAM, writeUri),
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

    private fun actionPendingIntent(requestCodeIndex: Int, associatedRequestCodes: ArrayList<Int>, notificationId: Int, wrappedIntent: Intent): PendingIntent =
        PendingIntent.getService(
            this,
            associatedRequestCodes[requestCodeIndex],
            Intent(this, OnPendingIntentService::class.java)
                .putClientExtras(notificationId, associatedRequestCodes)
                .putExtra(
                    WRAPPED_INTENT,
                    wrappedIntent
                        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                )
                .putExtra(CANCEL_NOTIFICATION, true),
            PendingIntent.FLAG_UPDATE_CURRENT
        )
}