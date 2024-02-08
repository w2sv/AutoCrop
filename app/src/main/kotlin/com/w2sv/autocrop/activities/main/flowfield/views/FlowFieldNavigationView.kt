package com.w2sv.autocrop.activities.main.flowfield.views

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
import com.w2sv.androidutils.generic.appPlayStoreUrl
import com.w2sv.androidutils.generic.openUrlWithActivityNotFoundHandling
import com.w2sv.androidutils.generic.requireActivity
import com.w2sv.androidutils.notifying.showToast
import com.w2sv.androidutils.permissions.permissionhandler.requestPermissions
import com.w2sv.androidutils.ui.dialogs.show
import com.w2sv.androidutils.ui.views.configureItem
import com.w2sv.androidutils.ui.views.viewModel
import com.w2sv.autocrop.R
import com.w2sv.autocrop.activities.ViewBoundFragmentActivity
import com.w2sv.autocrop.activities.main.about.AboutFragment
import com.w2sv.autocrop.activities.main.flowfield.CropSettingsDialogFragment
import com.w2sv.autocrop.activities.main.flowfield.FlowFieldFragment
import com.w2sv.screenshotlistening.ScreenshotListener

class FlowFieldNavigationView(context: Context, attributeSet: AttributeSet) :
    NavigationView(context, attributeSet) {

    private val viewModel by viewModel<FlowFieldFragment.ViewModel>()

    private val flowFieldFragment by lazy {
        findFragment<FlowFieldFragment>()
    }

    private val lifecycleOwner by lazy {
        findViewTreeLifecycleOwner()!!
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        if (!isInEditMode) {
            setListenToScreenCapturesItem()
            setCurrentCropDirIdentifier()
            setOnClickListeners()
        }
    }

    private fun setListenToScreenCapturesItem() =
        menu.configureItem(R.id.main_menu_item_listen_to_screen_captures) {
            it.actionView = Switch(context)
                .apply {
                    viewModel.screenshotListenerRunning.observe(lifecycleOwner) { isRunning ->
                        isChecked = isRunning
                    }
                    setOnCheckedChangeListener { _, value ->
                        when (value) {
                            true -> flowFieldFragment
                                .screenshotListeningPermissionHandlers
                                .requestPermissions(
                                    onGranted = {
                                        ScreenshotListener.startService(context)
                                        viewModel.setScreenshotListenerRunning(true)
                                    },
                                    onDenied = {
                                        viewModel.setScreenshotListenerRunning(false)
                                    }
                                )

                            false -> {
                                ScreenshotListener.stopService(context)
                                viewModel.setScreenshotListenerRunning(false)
                            }
                        }
                    }
                }
        }

    private fun setCurrentCropDirIdentifier() {
        viewModel.cropSaveDirIdentifier.observe(lifecycleOwner) { cropSaveDirIdentifier ->
            menu.configureItem(R.id.main_menu_item_save_dir_display) { item ->
                item.title = cropSaveDirIdentifier
            }
        }
    }

    private fun setOnClickListeners() {
        setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.main_menu_item_save_dir -> {
                    flowFieldFragment.launchCropSaveDirSelection()
                }

                R.id.main_menu_item_configure_cropping_settings -> {
                    CropSettingsDialogFragment()
                        .show(flowFieldFragment.childFragmentManager)
                }

                R.id.main_menu_item_about -> {
                    (context.requireActivity() as ViewBoundFragmentActivity).fragmentReplacementTransaction(
                        fragment = AboutFragment(),
                        animated = true
                    )
                        .addToBackStack(null)
                        .commit()
                }

                R.id.main_menu_item_go_to_github -> {
                    context.openUrlWithActivityNotFoundHandling("https://github.com/w2sv/autocrop")
                }

                R.id.main_menu_item_rate_the_app -> {
                    try {
                        context.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(appPlayStoreUrl(context))
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
                        .setText("Check out AutoCrop!\n${appPlayStoreUrl(context)}")
                        .startChooser()
                }
            }

            flowFieldFragment.binding.drawerLayout.closeDrawer()
            true
        }
    }
}