package com.autocrop.activities.main.fragments.flowfield.views

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.AttributeSet
import android.widget.ImageView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.findFragment
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.autocrop.activities.main.MainActivity
import com.autocrop.activities.main.MainActivityViewModel
import com.autocrop.activities.main.fragments.about.AboutFragment
import com.autocrop.activities.main.fragments.flowfield.FlowFieldFragment
import com.autocrop.preferences.UriPreferences
import com.autocrop.retriever.activity.ActivityRetriever
import com.autocrop.retriever.activity.ContextBasedActivityRetriever
import com.autocrop.screencapturelistening.ScreenCaptureListener
import com.autocrop.utils.android.IMAGE_MIME_TYPE
import com.autocrop.utils.android.extensions.activityViewModelLazy
import com.autocrop.utils.android.extensions.ifNotInEditMode
import com.autocrop.utils.android.extensions.setItemSwitch
import com.autocrop.utils.android.extensions.show
import com.autocrop.utils.android.extensions.snacky
import com.google.android.material.navigation.NavigationView
import com.w2sv.autocrop.R

class FlowFragmentNavigationView(context: Context, attributeSet: AttributeSet):
    NavigationView(context, attributeSet),
    ActivityRetriever<MainActivity> by ContextBasedActivityRetriever(context){

    private val viewModel by activityViewModelLazy<MainActivityViewModel>()

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        ifNotInEditMode {
            if (viewModel.savedCropUris != null)
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

            setItemSwitch(
                R.id.main_menu_item_listen_to_screen_capture,
                "listenToScreenCapture"
            ){ isChecked ->
                if (isChecked)
                    WorkManager
                        .getInstance(context.applicationContext)
                        .enqueueUniqueWork(
                            "Unique work",
                            ExistingWorkPolicy.REPLACE,
                            OneTimeWorkRequestBuilder<ScreenCaptureListener>()
                                .addTag(ScreenCaptureListener.TAG)
                                .build()
                        )
                else
                    WorkManager.getInstance(context.applicationContext)
                        .getWorkInfosForUniqueWork("Unique work").get().forEach {
                            println("ScreenshotCaptureListener ${it.state.isFinished}")
                        }
            }
            setItemSwitch(R.id.main_menu_item_auto_scroll, "autoScroll")

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

    private fun launchCropSharingIntent(){
        context.startActivity(
            Intent.createChooser(
                Intent().apply {
                    action = Intent.ACTION_SEND_MULTIPLE
                    putParcelableArrayListExtra(Intent.EXTRA_STREAM, viewModel.savedCropUris)
                    type = IMAGE_MIME_TYPE
                },
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
        fragmentHostingActivity.replaceCurrentFragmentWith(
            AboutFragment(),
            flipRight = false,
            addToBackStack = true
        )
    }
}
