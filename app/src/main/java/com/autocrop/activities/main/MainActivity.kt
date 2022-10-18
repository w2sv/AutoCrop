package com.autocrop.activities.main

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableStringBuilder
import androidx.activity.OnBackPressedCallback
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.text.color
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import com.autocrop.activities.iodetermination.IODeterminationActivity
import com.autocrop.activities.main.fragments.about.AboutFragment
import com.autocrop.activities.main.fragments.flowfield.FlowFieldFragment
import com.autocrop.activities.iodetermination.IOSynopsis
import com.autocrop.preferences.BooleanPreferences
import com.autocrop.preferences.UriPreferences
import com.autocrop.screencapturelistening.services.ScreenCaptureListeningService
import com.autocrop.uicontroller.activity.ApplicationActivity
import com.autocrop.utils.android.PermissionHandler
import com.autocrop.utils.android.extensions.getParcelableArrayList
import com.autocrop.utils.android.extensions.getThemedColor
import com.autocrop.utils.android.extensions.show
import com.autocrop.utils.android.extensions.snacky
import com.autocrop.utils.android.requestPermissions
import com.autocrop.utils.kotlin.extensions.numericallyInflected
import com.w2sv.autocrop.R

class MainActivity :
    ApplicationActivity<FlowFieldFragment, MainActivityViewModel>(
        FlowFieldFragment::class,
        MainActivityViewModel::class,
        BooleanPreferences, UriPreferences) {

    companion object{
        const val EXTRA_SELECTED_IMAGE_URIS = "com.autocrop.extra.SELECTED_IMAGE_URIS"
        const val EXTRA_N_DISMISSED_IMAGES = "com.autocrop.extra.N_DISMISSED_IMAGES"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        screenshotListeningPermissions.forEach {
            it?.let {
                lifecycle.addObserver(it)
            }
        }
    }

    val screenshotListeningPermissions = listOf(
        PermissionHandler(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            this,
            "Media file access required for listening to screen captures",
            "Go to app settings and grant media file access for screen capture listening to work"
        ),
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            PermissionHandler(
                Manifest.permission.POST_NOTIFICATIONS,
                this,
                "If you don't allow for the posting of notifications AutoCrop can't inform you about croppable screenshots",
                "Go to app settings and enable notification posting for screen capture listening to work"
            )
        else
            null
    )

    override fun viewModelFactory(): ViewModelProvider.Factory =
        MainActivityViewModelFactory(
            ioSynopsis = intent.getByteArrayExtra(IODeterminationActivity.EXTRA_IO_SYNOPSIS)?.let {
                IOSynopsis.fromByteArray(it)
            },
            savedCropUris = intent.getParcelableArrayList(IODeterminationActivity.EXTRA_CROP_SAVING_URIS)
        )

    override fun onSavedInstanceStateNull() {
        super.onSavedInstanceStateNull()

        onButtonsHalfFadedIn {
            if (!BooleanPreferences.welcomeDialogShown){
                supportFragmentManager.setFragmentResultListener(
                    ScreenshotListenerExplanation.REQUEST_KEY,
                    this
                ){_, bundle ->
                    if (ScreenshotListenerExplanation.dialogConfirmed(bundle))
                        screenshotListeningPermissions
                            .iterator()
                            .requestPermissions(
                                onGranted = {
                                    startService(Intent(this, ScreenCaptureListeningService::class.java))
                                }
                            )
                }
                CropExplanation().show(supportFragmentManager)
            }
            else
                viewModel.ioSynopsis?.let {
                    showIOSynopsisSnackbar(it)
                }
        }
    }

    private fun showIOSynopsisSnackbar(ioSynopsis: IOSynopsis){
        with(ioSynopsis){
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
            getCurrentFragment().let {
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