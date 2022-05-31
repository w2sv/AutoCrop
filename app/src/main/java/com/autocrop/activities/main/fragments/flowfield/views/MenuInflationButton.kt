package com.autocrop.activities.main.fragments.flowfield.views

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageButton
import com.autocrop.activities.main.MainActivity
import com.autocrop.activities.main.MainActivityViewModel
import com.autocrop.activities.main.fragments.about.AboutFragment
import com.autocrop.activities.main.fragments.flowfield.FlowFieldFragmentMenu
import com.autocrop.global.CropSavingPreferences
import com.autocrop.uicontroller.ViewModelRetriever
import com.autocrop.uielements.view.ActivityRetriever
import com.autocrop.uielements.view.ContextBasedActivityRetriever
import com.autocrop.utilsandroid.show
import com.autocrop.utilsandroid.snacky
import com.w2sv.autocrop.R

class MenuInflationButton(context: Context, attributeSet: AttributeSet) :
    AppCompatImageButton(context, attributeSet),
    ActivityRetriever<MainActivity> by ContextBasedActivityRetriever(context),
    ViewModelRetriever<MainActivityViewModel> by MainActivityViewModelRetriever(context) {

    init {
        setOnClickListener { popupMenu.show() }
    }

    private val popupMenu by lazy {
        FlowFieldFragmentMenu(
            mapOf(
                R.id.main_menu_item_change_save_destination_dir to ::pickCropSaveDestinationDir,
                R.id.main_menu_item_rate_the_app to ::goToPlayStoreListing,
                R.id.main_menu_item_about_the_app to ::invokeAboutFragment
            ),
            activity,
            this
        )
    }

    private fun pickCropSaveDestinationDir() =
        sharedViewModel.pickSaveDestinationDir.launch(CropSavingPreferences.treeUri)

    private fun goToPlayStoreListing() =
        try{
            activity.startActivity(
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

    private fun invokeAboutFragment() =
        typedActivity.replaceCurrentFragmentWith(
            AboutFragment(),
            flipRight = false,
            addToBackStack = true
        )
}