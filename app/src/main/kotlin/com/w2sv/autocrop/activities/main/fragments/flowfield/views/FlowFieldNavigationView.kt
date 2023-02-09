package com.w2sv.autocrop.activities.main.fragments.flowfield.views

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.AttributeSet
import android.widget.Switch
import androidx.core.app.ShareCompat
import androidx.fragment.app.findFragment
import androidx.lifecycle.findViewTreeLifecycleOwner
import com.google.android.material.navigation.NavigationView
import com.w2sv.androidutils.ActivityRetriever
import com.w2sv.androidutils.extensions.configureItem
import com.w2sv.androidutils.extensions.openUrl
import com.w2sv.androidutils.extensions.playStoreUrl
import com.w2sv.androidutils.extensions.serviceRunning
import com.w2sv.androidutils.extensions.showToast
import com.w2sv.androidutils.extensions.toggle
import com.w2sv.androidutils.extensions.viewModel
import com.w2sv.autocrop.R
import com.w2sv.autocrop.activities.ViewBoundFragmentActivity
import com.w2sv.autocrop.activities.main.fragments.about.AboutFragment
import com.w2sv.autocrop.activities.main.fragments.flowfield.FlowFieldFragment
import com.w2sv.permissionhandler.requestPermissions
import com.w2sv.preferences.BooleanPreferences
import com.w2sv.preferences.CropSaveDirPreferences
import com.w2sv.preferences.getConnectedSwitch
import com.w2sv.screenshotlistening.ScreenshotListener
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FlowFieldNavigationView(context: Context, attributeSet: AttributeSet) :
    NavigationView(context, attributeSet),
    ActivityRetriever by ActivityRetriever.Impl(context) {

    @Inject
    lateinit var booleanPreferences: BooleanPreferences

    @Inject
    lateinit var cropSaveDirPreferences: CropSaveDirPreferences

    private val viewModel by viewModel<FlowFieldFragment.ViewModel>()

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
                            viewModel.screenshotListenerCancelledFromNotificationLive.value == true -> viewModel.screenshotListenerCancelledFromNotificationLive.toggle()

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

                    viewModel.screenshotListenerCancelledFromNotificationLive.observe(this@FlowFieldNavigationView.findViewTreeLifecycleOwner()!!) { isCancelled ->
                        if (isCancelled)
                            isChecked = false
                    }
                }
        }

    private fun setCurrentCropDirIdentifier() {
        viewModel.cropSaveDirIdentifierLive.observe(findViewTreeLifecycleOwner()!!) { cropSaveDirIdentifier ->
            menu.configureItem(R.id.main_menu_item_current_crop_dir) { item ->
                item.title = cropSaveDirIdentifier
            }
        }
    }

    private fun setAutoScrollItem() =
        menu.configureItem(R.id.main_menu_item_auto_scroll) {
            it.actionView = booleanPreferences::autoScroll.getConnectedSwitch(context)
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
                    (activity as ViewBoundFragmentActivity).fragmentReplacementTransaction(
                        AboutFragment(),
                        animated = true,
                    )
                        .addToBackStack(null)
                        .commit()
                }

                R.id.main_menu_item_go_to_github -> {
                    context
                        .openUrl("https://github.com/w2sv/autocrop")
                }

                R.id.main_menu_item_rate_the_app -> {
                    try {
                        context.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(activity.playStoreUrl)
                            )
                                .setPackage("com.android.vending")
                        )
                    }
                    catch (e: ActivityNotFoundException) {
                        context.showToast("Seems like you're not signed into the Play Store \uD83E\uDD14")
                    }
                }

                R.id.main_menu_item_share -> {
                    ShareCompat.IntentBuilder(context)
                        .setType("text/plain")
                        .setText("Check out AutoCrop!\n${context.playStoreUrl}")
                        .startChooser()
                }
            }

            findFragment<FlowFieldFragment>().binding.drawerLayout.closeDrawer()
            false
        }
    }
}