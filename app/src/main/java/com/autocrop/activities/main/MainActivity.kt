package com.autocrop.activities.main

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.database.ContentObserver
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.text.SpannableStringBuilder
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.app.NotificationCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.text.color
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.autocrop.activities.IntentExtraIdentifier
import com.autocrop.activities.main.fragments.about.AboutFragment
import com.autocrop.activities.main.fragments.flowfield.FlowFieldFragment
import com.autocrop.dataclasses.IOSynopsis
import com.autocrop.preferences.BooleanPreferences
import com.autocrop.preferences.UriPreferences
import com.autocrop.ui.controller.activity.ApplicationActivity
import com.autocrop.utils.android.extensions.getThemedColor
import com.autocrop.utils.android.extensions.queryMediaStoreColumns
import com.autocrop.utils.android.extensions.show
import com.autocrop.utils.android.extensions.snacky
import com.autocrop.utils.kotlin.BlankFun
import com.autocrop.utils.kotlin.extensions.numericallyInflected
import com.google.android.play.core.review.ReviewManagerFactory
import com.w2sv.autocrop.R
import timber.log.Timber

class ScreenCaptureListenerWorker(appContext: Context, workerParams: WorkerParameters)
    : Worker(appContext, workerParams) {

    override fun doWork(): Result {
        createContentObserverFlow()
        return Result.success()
    }

    private fun createContentObserverFlow(){
        val contentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
            var shownUri: Uri? = null
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                uri?.let {
                    if (shownUri != it)
                        showNotification()
                    shownUri = it
                }
            }
        }
        applicationContext.contentResolver
            ?.registerContentObserver(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                true,
                contentObserver
            )
//        awaitClose {
//            applicationContext.contentResolver
//                ?.unregisterContentObserver(contentObserver)
//        }
    }

    private fun onContentChanged(uri: Uri) {
        val path = getFilePathFromContentResolver(applicationContext, uri)
        if (isScreenshotPath(path)) {
            showNotification()
        }
    }

    private fun showNotification() {
        val channelId = "channel_name"
        val notificationBuilder: NotificationCompat.Builder =
            NotificationCompat.Builder(applicationContext, channelId)
                .setSmallIcon(R.mipmap.logo)
                .setContentTitle("Title")
                .setContentText("Took screenshot")
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val mChannel = NotificationChannel(channelId, "sad", importance)
            notificationManager.createNotificationChannel(mChannel)
        }

        notificationManager.notify(69, notificationBuilder.build())
    }

    private fun isScreenshotPath(path: String?): Boolean {
        val lowercasePath = path?.lowercase()
        val screenshotDirectory = getPublicScreenshotDirectoryName()?.lowercase()
        return (screenshotDirectory != null &&
                lowercasePath?.contains(screenshotDirectory) == true) ||
                lowercasePath?.contains("screenshot") == true
    }

    private fun getPublicScreenshotDirectoryName() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_SCREENSHOTS).name
    } else null

    private fun getFilePathFromContentResolver(context: Context, uri: Uri): String {
        return context.contentResolver.queryMediaStoreColumns(uri, arrayOf(MediaStore.Images.Media.DISPLAY_NAME))[0]
    }
}

class MainActivity :
    ApplicationActivity<FlowFieldFragment, MainActivityViewModel>(
        FlowFieldFragment::class,
        MainActivityViewModel::class,
        BooleanPreferences, UriPreferences) {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        WorkManager
            .getInstance(this)
            .enqueue(
                OneTimeWorkRequestBuilder<ScreenCaptureListenerWorker>()
                    .build()
            )
    }

    override fun viewModelFactory(): ViewModelProvider.Factory =
        MainActivityViewModelFactory(
            ioSynopsis = getIntentExtra<ByteArray>(IntentExtraIdentifier.IO_SYNOPSIS)?.let {
                IOSynopsis.fromByteArray(it)
            },
            savedCropUris = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                intent.extras?.getParcelableArrayList(IntentExtraIdentifier.CROP_SAVING_URIS, Uri::class.java)
            else
                @Suppress("DEPRECATION")
                intent.extras?.getParcelableArrayList(IntentExtraIdentifier.CROP_SAVING_URIS)
        )

    override fun onSavedInstanceStateNull() {
        super.onSavedInstanceStateNull()

        if (!BooleanPreferences.welcomeMessageShown)
            onButtonsHalfFadedIn{
                snacky(
                    "Good to have you on board! \uD83D\uDD25 Now go ahead select some screenshots and save your first AutoCrops! \uD83D\uDE80",
                    duration = resources.getInteger(R.integer.duration_snackbar_extra_long)
                )
                    .show()
            }
        else{
            viewModel.ioSynopsis?.run {
                val showAsSnackbarOnButtonsHalfFadedIn: BlankFun = { onButtonsHalfFadedIn { showAsSnackbar() } }

                if (nSavedCrops != 0)
                    launchReviewFlow(showAsSnackbarOnButtonsHalfFadedIn)
                else
                    showAsSnackbarOnButtonsHalfFadedIn()
            }
        }
    }

    private fun launchReviewFlow(onFinishedListener: BlankFun){
        with(ReviewManagerFactory.create(this)){
            requestReviewFlow()
                .addOnCompleteListener { task ->
                    task.result?.let {
                        launchReviewFlow(this@MainActivity, it)
                            .addOnCompleteListener{ onFinishedListener() }
                    } ?: run {
                        Timber.i(task.exception)
                        onFinishedListener()
                    }
            }
        }
    }

    private fun IOSynopsis.showAsSnackbar(){
        val (text, icon) = if (nSavedCrops == 0)
            "Discarded all crops" to R.drawable.ic_outline_sentiment_dissatisfied_24
        else
            SpannableStringBuilder().apply {
                append("Saved $nSavedCrops ${"crop".numericallyInflected(nSavedCrops)} to ")
                color(getThemedColor(R.color.notification_success)) {append(cropWriteDirIdentifier)}
                if (nDeletedScreenshots != 0)
                    append(
                        " and deleted ${
                            if (nDeletedScreenshots == nSavedCrops)
                                "corresponding"
                            else
                                nDeletedScreenshots
                        } ${"screenshot".numericallyInflected(nDeletedScreenshots)}"
                    )
            } to R.drawable.ic_baseline_done_24

        snacky(text)
            .setIcon(icon)
            .show()
    }

    private fun onButtonsHalfFadedIn(runnable: Runnable){
        Handler(Looper.getMainLooper()).postDelayed(
            runnable,
            resources.getInteger(R.integer.duration_fade_in_flowfield_fragment_buttons).toLong() / 2
        )
    }

    /**
     * invoke [FlowFieldFragment] if [AboutFragment] showing, otherwise exit app
     */
    override val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            currentFragment().let {
                if (it is AboutFragment)
                    return supportFragmentManager.popBackStack()
                (it as? FlowFieldFragment)?.binding?.drawerLayout?.run {
                    if (isOpen)
                        return closeDrawer(GravityCompat.START)
                }
                finishAffinity()
            }
        }
    }
}