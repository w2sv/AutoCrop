package com.w2sv.autocrop.activities.main.fragments.flowfield.views

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.AttributeSet
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.Switch
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.findFragment
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.navigation.NavigationView
import com.w2sv.androidutils.extensions.configureItem
import com.w2sv.androidutils.extensions.getColoredIcon
import com.w2sv.androidutils.extensions.goToWebpage
import com.w2sv.androidutils.extensions.hiltActivityViewModel
import com.w2sv.androidutils.extensions.ifNotInEditMode
import com.w2sv.androidutils.extensions.postValue
import com.w2sv.androidutils.extensions.serviceRunning
import com.w2sv.autocrop.R
import com.w2sv.autocrop.activities.main.MainActivity
import com.w2sv.autocrop.activities.main.fragments.about.AboutFragment
import com.w2sv.autocrop.activities.main.fragments.flowfield.FlowFieldFragment
import com.w2sv.autocrop.activities.FragmentHostingActivity
import com.w2sv.autocrop.preferences.BooleanPreferences
import com.w2sv.autocrop.preferences.UriPreferences
import com.w2sv.autocrop.screenshotlistening.services.ScreenshotListener
import com.w2sv.autocrop.utils.IMAGE_MIME_TYPE
import com.w2sv.autocrop.utils.extensions.snackyBuilder
import com.w2sv.permissionhandler.requestPermissions
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FlowFieldNavigationView(context: Context, attributeSet: AttributeSet) :
    NavigationView(context, attributeSet),
    FragmentHostingActivity.Retriever by FragmentHostingActivity.Retriever.Implementation(context) {

    @Inject
    lateinit var booleanPreferences: BooleanPreferences

    @Inject
    lateinit var uriPreferences: UriPreferences

    private val activityViewModel by hiltActivityViewModel<MainActivity.ViewModel>()

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        ifNotInEditMode {
            setShareCropsItem()
            setListenToScreenCapturesItem()
            setAutoScrollItem()

            setNavigationItemSelectedListeners()
        }
    }

    private fun setShareCropsItem() =
        menu.configureItem(R.id.main_menu_item_share_crops) {
            activityViewModel.ioResults?.let { results ->
                if (results.nSavedCrops != 0) {
                    it.isVisible = true
                    it.actionView = ImageView(context).apply {
                        setImageDrawable(
                            context.getColoredIcon(R.drawable.ic_priority_high_24, R.color.highest_light)
                        )
                    }
                }
            }
        }

    private fun setListenToScreenCapturesItem() =
        menu.configureItem(R.id.main_menu_item_listen_to_screen_captures) {
            it.actionView = Switch(context)
                .apply {
                    isChecked = context.serviceRunning<ScreenshotListener>()

                    val onCheckedChangeListener =
                        CompoundButton.OnCheckedChangeListener { _, newValue ->
                            if (newValue) {
                                findFragment<FlowFieldFragment>()
                                    .screenshotListeningPermissions
                                    .iterator()
                                    .requestPermissions(
                                        onGranted = {
                                            ScreenshotListener.startService(context)
                                        },
                                        onDenied = {
                                            isChecked = false
                                        }
                                    )
                            }
                            else
                                ScreenshotListener.stopService(context)
                        }

                    activityViewModel.liveScreenshotListenerRunning.let { liveData ->
                        liveData.observe(activity as LifecycleOwner) { cancelledOptional ->
                            cancelledOptional?.let { cancelled ->
                                setOnCheckedChangeListener(null)
                                isChecked = cancelled
                                setOnCheckedChangeListener(onCheckedChangeListener)
                                liveData.postValue(null)
                            }
                        }
                    }

                    setOnCheckedChangeListener(onCheckedChangeListener)
                }
        }

    private fun setAutoScrollItem() =
        menu.configureItem(R.id.main_menu_item_auto_scroll) {
            it.actionView = booleanPreferences.createSwitch(context, "autoScroll")
        }

    private fun setNavigationItemSelectedListeners() {
        setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.main_menu_item_share_crops -> launchCropSharingIntent()
                R.id.main_menu_item_change_crop_saving_dir -> pickSaveDestinationDir()
                R.id.main_menu_item_about -> invokeAboutFragment()
                R.id.main_menu_item_go_to_github -> goToGithub()
                R.id.main_menu_item_rate_the_app -> goToPlayStoreListing()
            }
            (parent as DrawerLayout).closeDrawer(GravityCompat.START)
            return@setNavigationItemSelectedListener false
        }
    }

    private fun launchCropSharingIntent() {
        context.startActivity(
            Intent.createChooser(
                Intent(Intent.ACTION_SEND_MULTIPLE)
                    .putExtra(
                        Intent.EXTRA_STREAM,
                        activityViewModel.ioResults!!.cropUris
                    )
                    .setType(IMAGE_MIME_TYPE),
                null
            )
        )
    }

    private fun pickSaveDestinationDir() {
        findFragment<FlowFieldFragment>()
            .saveDestinationSelectionIntentLauncher
            .launch(uriPreferences.treeUri)
    }

    private fun goToPlayStoreListing() {
        try {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=${activity.packageName}")
                )
                    .setPackage("com.android.vending")
            )
        }
        catch (e: ActivityNotFoundException) {
            activity
                .snackyBuilder("Seems like you're not signed into the Play Store, pal \uD83E\uDD14")
                .build()
                .show()
        }
    }

    private fun goToGithub() {
        context
            .goToWebpage("https://github.com/w2sv/autocrop")
    }

    private fun invokeAboutFragment() {
        fragmentHostingActivity.fragmentReplacementTransaction(
            AboutFragment(),
            animated = true,
        )
            .addToBackStack(null)
            .commit()
    }
}