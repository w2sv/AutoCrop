package com.autocrop.activities.main.fragments.flowfield.views

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.net.Uri
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.Switch
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.findFragment
import com.autocrop.activities.main.MainActivity
import com.autocrop.activities.main.MainActivityViewModel
import com.autocrop.activities.main.fragments.about.AboutFragment
import com.autocrop.activities.main.fragments.flowfield.FlowFieldFragment
import com.autocrop.preferences.BooleanPreferences
import com.autocrop.preferences.UriPreferences
import com.autocrop.screencapturelistening.services.ScreenshotListener
import com.autocrop.uicontroller.activity.retriever.ActivityRetriever
import com.autocrop.uicontroller.activity.retriever.ContextBasedActivityRetriever
import com.autocrop.utils.android.IMAGE_MIME_TYPE
import com.autocrop.utils.android.extensions.activityViewModelLazy
import com.autocrop.utils.android.extensions.ifNotInEditMode
import com.autocrop.utils.android.extensions.serviceRunning
import com.autocrop.utils.android.extensions.show
import com.autocrop.utils.android.extensions.snacky
import com.autocrop.utils.android.requestPermissions
import com.google.android.material.navigation.NavigationView
import com.w2sv.autocrop.R

class FlowFieldNavigationView(context: Context, attributeSet: AttributeSet):
    NavigationView(context, attributeSet),
    ActivityRetriever<MainActivity> by ContextBasedActivityRetriever(context) {

    private val viewModel by activityViewModelLazy<MainActivityViewModel>()

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        (menu.findItem(R.id.main_menu_item_listen_to_screen_captures).actionView as Switch)
            .isChecked = context.serviceRunning<ScreenshotListener>()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        ifNotInEditMode {
            setShareCropsItem()
            setListenToScreenCapturesItem()
            setAutoScrollItem()

            setNavigationItemSelectedListener {
                when (it.itemId){
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
    }

    private fun setShareCropsItem(){
        viewModel.ioResults?.let {
            if (it.nSavedCrops != 0)
                with(menu.findItem(R.id.main_menu_item_share_crops)){
                    isVisible = true
                    actionView = ImageView(context).apply {
                        setImageDrawable(
                            AppCompatResources.getDrawable(
                                context,
                                R.drawable.ic_baseline_priority_high_24
                            )
                        )
                    }
                }
        }
    }

    private fun setListenToScreenCapturesItem(){
        menu.findItem(R.id.main_menu_item_listen_to_screen_captures).actionView = Switch(context)
            .apply {
                setOnCheckedChangeListener{ _, newValue ->
                    if (newValue) {
                        typedActivity
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
            }
    }

    private fun setAutoScrollItem(){
        menu.findItem(R.id.main_menu_item_auto_scroll)
            .actionView = BooleanPreferences.createSwitch(context, "autoScroll")
    }

    private fun launchCropSharingIntent(){
        context.startActivity(
            Intent.createChooser(
                Intent(Intent.ACTION_SEND_MULTIPLE)
                    .putExtra(
                        Intent.EXTRA_STREAM,
                        viewModel.ioResults!!.cropUris
                    )
                    .setType(IMAGE_MIME_TYPE),
                null
            )
        )
    }

    private fun pickSaveDestinationDir(){
        findFragment<FlowFieldFragment>()
            .saveDestinationSelectionIntentLauncher
            .launch(UriPreferences.treeUri)
    }

    private fun goToPlayStoreListing(){
        try{
            context.startActivity(
                Intent(Intent.ACTION_VIEW).apply {
                    data =
                        Uri.parse("https://play.google.com/store/apps/details?id=${activity.packageName}")
                    setPackage("com.android.vending")
                }
            )
        } catch (e: ActivityNotFoundException){
            activity
                .snacky("Seems like you're not signed into the Play Store, pal \uD83E\uDD14")
                .show()
        }
    }

    private fun goToGithub(){
        context.startActivity(
            Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://github.com/w2sv/autocrop")
            }
        )
    }

    private fun invokeAboutFragment(){
        fragmentHostingActivity.fragmentReplacementTransaction(
            AboutFragment(),
            flipRight = true,
        )
            .addToBackStack(null)
            .commit()
    }
}