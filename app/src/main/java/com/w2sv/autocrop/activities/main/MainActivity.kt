package com.w2sv.autocrop.activities.main

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.text.SpannableStringBuilder
import androidx.activity.OnBackPressedCallback
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.text.color
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import com.w2sv.autocrop.R
import com.w2sv.autocrop.activities.iodetermination.IODeterminationActivity
import com.w2sv.autocrop.activities.main.fragments.about.AboutFragment
import com.w2sv.autocrop.activities.main.fragments.flowfield.FlowFieldFragment
import com.w2sv.autocrop.controller.activity.ApplicationActivity
import com.w2sv.autocrop.preferences.BooleanPreferences
import com.w2sv.autocrop.preferences.UriPreferences
import com.w2sv.autocrop.screenshotlistening.services.ScreenshotListener
import com.w2sv.autocrop.utils.android.extensions.getLong
import com.w2sv.autocrop.utils.android.extensions.getThemedColor
import com.w2sv.autocrop.utils.android.extensions.show
import com.w2sv.autocrop.utils.android.extensions.snackyBuilder
import com.w2sv.autocrop.utils.android.postDelayed
import com.w2sv.kotlinutils.extensions.numericallyInflected
import com.w2sv.permissionhandler.PermissionHandler

class MainActivity :
    ApplicationActivity<FlowFieldFragment, MainActivityViewModel>(
        FlowFieldFragment::class.java,
        MainActivityViewModel::class.java,
        BooleanPreferences, UriPreferences
    ),
    CropExplanation.OnDismissListener,
    ScreenshotListenerExplanation.OnConfirmedListener{

    companion object {
        const val EXTRA_SELECTED_IMAGE_URIS = "com.w2sv.autocrop.extra.SELECTED_IMAGE_URIS"
        const val EXTRA_N_DISMISSED_IMAGES = "com.w2sv.autocrop.extra.N_DISMISSED_IMAGES"
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                Manifest.permission.READ_MEDIA_IMAGES
            else
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
        MainActivityViewModel.Factory(
            ioResults = IODeterminationActivity.Results.attemptRestoration(intent)
        )

    override fun onSavedInstanceStateNull() {
        super.onSavedInstanceStateNull()

        if (!BooleanPreferences.welcomeDialogShown)
            postDelayed(resources.getLong(R.integer.duration_flowfield_buttons_fade_in_halve)) {
                CropExplanation().show(supportFragmentManager)
            }
        else if (viewModel.ioResults != null)
            postDelayed(resources.getLong(R.integer.duration_flowfield_buttons_fade_in_halve)){
                showIOSynopsisSnackbar(viewModel.ioResults!!)
            }
    }

    override fun onDismiss() {
        ScreenshotListenerExplanation().show(supportFragmentManager)
    }

    override fun onConfirmed() {
        ScreenshotListener.startService(this)
    }

    private fun showIOSynopsisSnackbar(ioResults: IODeterminationActivity.Results) {
        with(ioResults) {
            val (text, icon) = if (nSavedCrops == 0)
                "Discarded all crops" to R.drawable.ic_outline_sentiment_dissatisfied_24
            else
                SpannableStringBuilder().apply {
                    append("Saved $nSavedCrops ${"crop".numericallyInflected(nSavedCrops)} to ")
                    color(getThemedColor(R.color.success)) { append(saveDirName) }
                    if (nDeletedScreenshots != 0)
                        append(
                            " and deleted ${
                                if (nDeletedScreenshots == nSavedCrops)
                                    "corresponding"
                                else
                                    nDeletedScreenshots
                            } ${"screenshot".numericallyInflected(nDeletedScreenshots)}"
                        )
                } to R.drawable.ic_check_green_24

            snackyBuilder(text)
                .setIcon(icon)
                .build().show()
        }
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