package com.w2sv.autocrop.activities.main.fragments.flowfield.views

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.AttributeSet
import android.widget.Switch
import androidx.core.app.ShareCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.findFragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.findViewTreeLifecycleOwner
import com.google.android.material.navigation.NavigationView
import com.w2sv.androidutils.extensions.configureItem
import com.w2sv.androidutils.extensions.goToWebpage
import com.w2sv.androidutils.extensions.hiltActivityViewModel
import com.w2sv.androidutils.extensions.postValue
import com.w2sv.androidutils.extensions.serviceRunning
import com.w2sv.androidutils.extensions.viewModel
import com.w2sv.autocrop.R
import com.w2sv.autocrop.activities.FragmentedActivity
import com.w2sv.autocrop.activities.main.MainActivity
import com.w2sv.autocrop.activities.main.fragments.about.AboutFragment
import com.w2sv.autocrop.activities.main.fragments.flowfield.FlowFieldFragment
import com.w2sv.autocrop.preferences.BooleanPreferences
import com.w2sv.autocrop.preferences.CropSaveDirPreferences
import com.w2sv.autocrop.screenshotlistening.ScreenshotListener
import com.w2sv.autocrop.utils.extensions.snackyBuilder
import com.w2sv.permissionhandler.requestPermissions
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FlowFieldNavigationView(context: Context, attributeSet: AttributeSet) :
    NavigationView(context, attributeSet),
    FragmentedActivity.Retriever by FragmentedActivity.Retriever.Implementation(context) {

    @Inject
    lateinit var booleanPreferences: BooleanPreferences

    @Inject
    lateinit var cropSaveDirPreferences: CropSaveDirPreferences

    private val viewModel by viewModel<FlowFieldFragment.ViewModel>()
    private val activityViewModel by hiltActivityViewModel<MainActivity.ViewModel>()

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        if (!isInEditMode) {
            setListenToScreenCapturesItem()
            setCurrentCropDirIdentifier()
            setAutoScrollItem()
            setOnClickListeners()
        }
    }

    private fun setListenToScreenCapturesItem() =
        menu.configureItem(R.id.main_menu_item_listen_to_screen_captures) {
            it.actionView = Switch(context)
                .apply {
                    isChecked = context.serviceRunning<ScreenshotListener>()
                    setOnCheckedChangeListener { _, value ->
                        when {
                            activityViewModel.liveScreenshotListenerRunning.value != null -> activityViewModel.liveScreenshotListenerRunning.postValue(
                                null
                            )

                            value -> findFragment<FlowFieldFragment>()
                                .screenshotListeningPermissionHandlers
                                .requestPermissions(
                                    onGranted = {
                                        ScreenshotListener.startService(context)
                                    },
                                    onDenied = {
                                        isChecked = false
                                    }
                                )

                            else -> ScreenshotListener.stopService(context)
                        }
                    }

                    activityViewModel.liveScreenshotListenerRunning.observe(activity as LifecycleOwner) { isRunningOptional ->
                        isRunningOptional?.let { isRunning ->
                            isChecked = isRunning
                        }
                    }
                }
        }

    private fun setCurrentCropDirIdentifier() {
        viewModel.liveCropSaveDirIdentifier.observe(findViewTreeLifecycleOwner()!!) { cropSaveDirIdentifier ->
            menu.configureItem(R.id.main_menu_item_current_crop_dir) { item ->
                item.title = cropSaveDirIdentifier
            }
        }
    }

    private fun setAutoScrollItem() =
        menu.configureItem(R.id.main_menu_item_auto_scroll) {
            it.actionView = booleanPreferences.createSwitch(context, "autoScroll")
        }

    private fun setOnClickListeners() {
        setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.main_menu_item_change_crop_dir -> {
                    findFragment<FlowFieldFragment>()
                        .openDocumentTreeContractHandler
                        .selectDocument(cropSaveDirPreferences.treeUri)
                }

                R.id.main_menu_item_about -> {
                    fragmentedActivity.fragmentReplacementTransaction(
                        AboutFragment(),
                        animated = true,
                    )
                        .addToBackStack(null)
                        .commit()
                }

                R.id.main_menu_item_go_to_github -> {
                    context
                        .goToWebpage("https://github.com/w2sv/autocrop")
                }

                R.id.main_menu_item_rate_the_app -> {
                    try {
                        context.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(playStoreLink())
                            )
                                .setPackage("com.android.vending")
                        )
                    }
                    catch (e: ActivityNotFoundException) {
                        activity
                            .snackyBuilder("Seems like you're not signed into the Play Store \uD83E\uDD14")
                            .build()
                            .show()
                    }
                }

                R.id.main_menu_item_share -> {
                    ShareCompat.IntentBuilder(context)
                        .setType("text/plain")
                        .setText("Check out AutoCrop!\n\n${playStoreLink()}")
                        .setChooserTitle("Choose an app")
                        .startChooser()
                }
            }

            (parent as DrawerLayout).closeDrawer(GravityCompat.START)
            false
        }
    }

    private fun playStoreLink(): String =
        "https://play.google.com/store/apps/details?id=${activity.packageName}"
}