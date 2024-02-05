package com.w2sv.screenshotlistening

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.database.CursorIndexOutOfBoundsException
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.core.app.NotificationCompat
import com.google.common.collect.EvictingQueue
import com.w2sv.common.PermissionHandler
import com.w2sv.cropbundle.io.CROP_FILE_ADDENDUM
import com.w2sv.cropbundle.io.IMAGE_DELETION_REQUIRING_APPROVAL
import com.w2sv.cropbundle.io.extensions.compressToAndCloseStream
import com.w2sv.cropbundle.io.extensions.loadBitmap
import com.w2sv.cropbundle.io.extensions.queryMediaStoreData
import com.w2sv.cropbundle.io.getImageDeleteRequestUri
import com.w2sv.cropbundle.io.utils.systemScreenshotsDirectory
import com.w2sv.domain.repository.PreferencesRepository
import com.w2sv.kotlinutils.dateFromUnixTimestamp
import com.w2sv.kotlinutils.extensions.getNextTriple
import com.w2sv.kotlinutils.timeDelta
import com.w2sv.screenshotlistening.notifications.AppNotificationChannel
import com.w2sv.screenshotlistening.notifications.NotificationGroup
import com.w2sv.screenshotlistening.notifications.setChannelAndGetNotificationBuilder
import com.w2sv.screenshotlistening.services.abstrct.BoundService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import slimber.log.i
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@AndroidEntryPoint
class ScreenshotListener : BoundService(),
                           PendingIntentAssociatedResourcesCleanupService.Client {

    @Inject
    lateinit var preferencesRepository: PreferencesRepository

    @Singleton
    class CancelledFromNotification @Inject constructor() {
        private val scope = CoroutineScope(Dispatchers.Main)

        val sharedFlow get() = mutableSharedFlow.asSharedFlow()
        private val mutableSharedFlow = MutableSharedFlow<Unit>()

        internal fun emit() {
            scope.launch { mutableSharedFlow.emit(Unit) }
        }
    }

    class CleanupService : PendingIntentAssociatedResourcesCleanupService<ScreenshotListener>(ScreenshotListener::class.java)

    @AndroidEntryPoint
    class OnCancelledFromNotificationListener : BroadcastReceiver() {

        @Inject
        lateinit var cancelledFromNotification: CancelledFromNotification

        override fun onReceive(context: Context?, intent: Intent?) {
            stopService(context!!)

            cancelledFromNotification.emit()
        }

        companion object {
            private const val PENDING_INTENT_REQUEST_CODE = 1447

            fun getPendingIntent(context: Context): PendingIntent =
                PendingIntent.getBroadcast(
                    context,
                    PENDING_INTENT_REQUEST_CODE,
                    Intent(context, OnCancelledFromNotificationListener::class.java),
                    PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
                )
        }
    }

    // =============
    // Launching
    // =============

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        emitOnStartCommandLog(intent, flags, startId)

        when (intent!!.action) {
            ACTION_STOP_SERVICE -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }

            else -> {
                startForeground(
                    AppNotificationChannel.STARTED_FOREGROUND_SERVICE.childIdSeed,
                    foregroundServiceNotificationBuilder()
                        .build()
                )
                    .also { i { "Started foreground service" } }

                contentResolver.registerContentObserver(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    true,
                    screenshotObserver
                )
                    .also { i { "Registered imageContentObserver" } }
            }
        }

        return START_REDELIVER_INTENT
    }

    private fun foregroundServiceNotificationBuilder(): NotificationCompat.Builder =
        setChannelAndGetNotificationBuilder(
            AppNotificationChannel.STARTED_FOREGROUND_SERVICE,
            AppNotificationChannel.STARTED_FOREGROUND_SERVICE.title
        )
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(getString(R.string.you_will_receive_a_notification_when_autocrop_detects_a_new_croppable_screenshot))
            )
            .addAction(
                NotificationCompat.Action(
                    com.w2sv.common.R.drawable.ic_cancel_24,
                    getString(R.string.stop),
                    OnCancelledFromNotificationListener.getPendingIntent(this)
                )
            )

    // ===========================================
    // Observing & Croppability determination
    // ===========================================

    private val screenshotObserver by lazy {
        ScreenshotObserver(contentResolver, ::onNewScreenshotUri)
    }

    /**
     * Catches [IllegalStateException] in case of [uri] not being accessible due to still pending
     * (meaning that the device screenshot manager (= uri owner) is still accessing/processing the file,
     * such that the uri is blocked for other processes).
     *
     * @return [Boolean] indicating whether [uri] was accessible, meaning that this function should not
     * be called for the same [uri] again.
     */
    private fun onNewScreenshotUri(uri: Uri): Boolean =
        try {
            val screenshotBitmap = contentResolver.loadBitmap(uri)!!
            //            screenshotBitmap.crop(preferencesRepository.edgeCandidateThreshold)?.let { cropResult ->
            //                val screenshotMediaStoreData = Screenshot.MediaStoreData.query(contentResolver, uri)
            //                val cropBitmap = screenshotBitmap.cropped(cropResult.edges)
            //                val temporaryCropFilePath = saveCropToTempFile(cropBitmap, screenshotMediaStoreData.id)
            //
            //                showCroppableScreenshotDetectedNotification(
            //                    cropBitmap,
            //                    screenshotMediaStoreData.id
            //                ) { clazz, isSaveIntent, notificationId, actionRequestCodes, putCancelNotificationExtra ->
            //                    Intent(this, clazz)
            //                        .putExtra(EXTRA_TEMPORARY_CROP_FILE_PATH, temporaryCropFilePath)
            //                        .putCleanupExtras(
            //                            notificationId,
            //                            actionRequestCodes,
            //                            putCancelNotificationExtra
            //                        )
            //                        .apply {
            //                            if (isSaveIntent) {
            //                                data = uri
            //                                putExtra(EXTRA_SCREENSHOT_MEDIASTORE_DATA, screenshotMediaStoreData)
            //                            }
            //                        }
            //                }
            //            }
            true
        }
        catch (ex: Exception) {
            when (ex) {
                is IllegalStateException, is NullPointerException, is FileNotFoundException -> Unit
                else -> throw ex
            }
            false
        }

    private fun saveCropToTempFile(crop: Bitmap, screenshotMediaStoreId: Long): String {
        val file = File.createTempFile(
            screenshotMediaStoreId.toString(),
            null
        )
        crop.compressToAndCloseStream(
            FileOutputStream(file),
            Bitmap.CompressFormat.PNG
        )
        return file.path
    }

    // ============
    // Notifying
    // ============

    private fun showCroppableScreenshotDetectedNotification(
        cropBitmap: Bitmap,
        screenshotMediaStoreId: Long,
        getActionIntentTemplate: (Class<*>, Boolean, Int, ArrayList<Int>, Boolean) -> Intent
    ) {
        val notificationId = notificationGroup.childrenIds.getNewId()
        val actionRequestCodes = notificationGroup.requestCodes.getAndAddMultipleNewIds(4)

        fun getActionIntent(clazz: Class<*>, isSaveIntent: Boolean, cancelNotificationExtra: Boolean): Intent =
            getActionIntentTemplate(
                clazz,
                isSaveIntent,
                notificationId,
                actionRequestCodes,
                cancelNotificationExtra
            )

        notificationGroup.addChild(notificationId) {
            setContentTitle(getString(R.string.crafted_a_new_autocrop))
            addAction(
                NotificationCompat.Action(
                    null,
                    getString(R.string.save),
                    PendingIntent.getService(
                        this@ScreenshotListener,
                        actionRequestCodes[0],
                        getActionIntent(CropIOService::class.java, isSaveIntent = true, cancelNotificationExtra = true),
                        REPLACE_CURRENT_PENDING_INTENT_FLAGS
                    )
                )
            )
            addAction(
                NotificationCompat.Action(
                    null,
                    getString(R.string.save_delete_screenshot),
                    if (IMAGE_DELETION_REQUIRING_APPROVAL)
                        PendingIntent.getActivity(
                            this@ScreenshotListener,
                            actionRequestCodes[1],
                            getActionIntent(
                                ScreenshotDeleteRequestActivity::class.java,
                                isSaveIntent = true,
                                cancelNotificationExtra = true
                            )
                                .putExtra(EXTRA_DELETE_REQUEST_URI, getImageDeleteRequestUri(screenshotMediaStoreId)),
                            REPLACE_CURRENT_PENDING_INTENT_FLAGS
                        )
                    else
                        PendingIntent.getService(
                            this@ScreenshotListener,
                            actionRequestCodes[1],
                            getActionIntent(
                                CropIOService::class.java,
                                isSaveIntent = true,
                                cancelNotificationExtra = true
                            )
                                .putExtra(EXTRA_ATTEMPT_SCREENSHOT_DELETION, true),
                            REPLACE_CURRENT_PENDING_INTENT_FLAGS
                        )
                )
            )
            addAction(
                NotificationCompat.Action(
                    null,
                    getString(R.string.dismiss),
                    PendingIntent.getService(
                        this@ScreenshotListener,
                        actionRequestCodes[2],
                        getActionIntent(
                            CleanupService::class.java,
                            isSaveIntent = false,
                            cancelNotificationExtra = true
                        ),
                        REPLACE_CURRENT_PENDING_INTENT_FLAGS
                    )
                )
            )
            setStyle(
                NotificationCompat.BigPictureStyle()
                    .bigPicture(cropBitmap)
            )
            setDeleteIntent(
                PendingIntent.getService(
                    this@ScreenshotListener,
                    actionRequestCodes[3],
                    getActionIntent(
                        CleanupService::class.java,
                        isSaveIntent = false,
                        cancelNotificationExtra = false
                    ),
                    REPLACE_CURRENT_PENDING_INTENT_FLAGS
                )
            )
        }
    }

    override val notificationGroup = NotificationGroup(
        this,
        notificationChannel = AppNotificationChannel.DETECTED_NEW_CROPPABLE_SCREENSHOT,
        summaryBuilderConfigurator = { nChildren ->
            setContentTitle(getString(R.string.detected_n_croppable_screenshots, nChildren))
            setStyle(
                NotificationCompat.InboxStyle()
                    .setSummaryText(getString(R.string.expand_to_select_actions))
            )
        }
    )

    // ============
    // Clean-up
    // ============

    override fun onCleanupFinishedListener(intent: Intent) {
        File(intent.getStringExtra(EXTRA_TEMPORARY_CROP_FILE_PATH)!!).delete()
    }

    override fun onDestroy() {
        super.onDestroy()

        contentResolver.unregisterContentObserver(screenshotObserver)
            .also { i { "Unregistered imageContentObserver" } }
    }

    companion object {
        fun startService(context: Context) {
            context.startService(
                getIntent(context)
            )
            i { "Starting ScreenshotListener" }
        }

        fun stopService(context: Context) {
            context.startService(
                getIntent(context)
                    .setAction(ACTION_STOP_SERVICE)
            )
            i { "Stopping ScreenshotListener" }
        }

        private const val ACTION_STOP_SERVICE = "com.w2sv.autocrop.STOP_SERVICE"

        private fun getIntent(context: Context): Intent =
            Intent(context, ScreenshotListener::class.java)

        fun startCleanupService(context: Context, intent: Intent) {
            context.startService(
                intent.setClass(context, CleanupService::class.java)
            )
        }

        const val EXTRA_ATTEMPT_SCREENSHOT_DELETION = "com.w2sv.autocrop.extra.DELETE_SCREENSHOT"
        const val EXTRA_DELETE_REQUEST_URI = "com.w2sv.autocrop.extra.DELETE_REQUEST_URI"
        const val EXTRA_SCREENSHOT_MEDIASTORE_DATA = "com.w2sv.autocrop.extra.SCREENSHOT_MEDIASTORE_DATA"
        const val EXTRA_TEMPORARY_CROP_FILE_PATH = "com.w2sv.autocrop.extra.CROP_FILE_PATH"

        fun permissionHandlers(componentActivity: ComponentActivity): List<PermissionHandler> =
            buildList {
                add(
                    PermissionHandler(
                        componentActivity,
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                            Manifest.permission.READ_MEDIA_IMAGES
                        else
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                        R.string.media_file_access_required_for_registering_new_screenshots,
                        R.string.go_to_app_settings_and_grant_media_file_access_for_screenshot_listening_to_work
                    )
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    add(
                        PermissionHandler(
                            componentActivity,
                            Manifest.permission.POST_NOTIFICATIONS,
                            R.string.if_you_don_t_allow_notification_posting_autocrop_can_t_inform_you_about_croppable_screenshots,
                            R.string.go_to_app_settings_and_enable_notification_posting_for_screenshot_listening_to_work
                        )
                    )
                }
            }
    }
}

private const val REPLACE_CURRENT_PENDING_INTENT_FLAGS: Int =
    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE

private class ScreenshotObserver(
    private val contentResolver: ContentResolver,
    private val onNewScreenshotListener: (Uri) -> Boolean
) : ContentObserver(Handler(Looper.getMainLooper())) {

    companion object {
        /**
         * Number of URIs, that could possibly require simultaneous processing
         */
        const val MAX_URIS = 3
    }

    /**
     * Uris which have already been processed & are not to be considered again.
     */
    private val blacklist = EvictingQueue.create<Uri>(MAX_URIS)

    /**
     * Uris that have been asserted to correspond to a screenshot, but which were still pending
     * during last check.
     */
    private val pendingUris = EvictingQueue.create<Uri>(MAX_URIS)

    /**
     * According to own observation called on each change to a matching Uri, including
     * changes to their [MediaStore.MediaColumns].
     */
    override fun onChange(selfChange: Boolean, uri: Uri?) {
        uri?.let {
            i { "Called onChange for $it" }
            if (!blacklist.contains(it)) {
                if (pendingUris.contains(it))
                    attemptOnNewScreenshotListenerInvocation(it, false)
                else {
                    when (it.isNewScreenshot()) {
                        true -> attemptOnNewScreenshotListenerInvocation(it, true)
                        false -> blacklist.add(it)
                        null -> pendingUris.add(it)
                    }
                }
            }
        }
    }

    private fun attemptOnNewScreenshotListenerInvocation(uri: Uri, addToPendingUrisIfInaccessible: Boolean) {
        if (onNewScreenshotListener(uri)) {
            blacklist.add(uri)
            i { "Added $uri to blacklist" }
        }
        else if (addToPendingUrisIfInaccessible)
            pendingUris.add(uri)
    }

    private fun Uri.isNewScreenshot(): Boolean? =
        try {
            val (absolutePath, fileName, dateAdded) = contentResolver.queryMediaStoreData(
                this,
                arrayOf(
                    MediaStore.Images.Media.DISPLAY_NAME,
                    MediaStore.Images.Media.DATA,  // e.g. /storage/emulated/0/Pictures/Screenshots/.pending-1665749333-Screenshot_20221007-140853687.png
                    MediaStore.Images.Media.DATE_ADDED
                )
            )
                .iterator()
                .getNextTriple()

            !fileName.contains(CROP_FILE_ADDENDUM) &&  // exclude produced AutoCrop's
                    timeDelta(  // exclude files having already been on the file system and which were only triggered, due to a change of their metadata
                        dateFromUnixTimestamp(dateAdded),
                        Date(System.currentTimeMillis()),
                        TimeUnit.SECONDS
                    ) < 20 &&
                    (systemScreenshotsDirectory()?.let { absolutePath.contains(it.name) } == true ||  // infer whether or not actually a screenshot
                            fileName
                                .lowercase()
                                .contains("screenshot"))

        }
        catch (e: CursorIndexOutOfBoundsException) {
            null
        }
}