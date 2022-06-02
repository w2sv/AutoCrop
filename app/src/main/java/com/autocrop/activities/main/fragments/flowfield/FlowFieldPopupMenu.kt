package com.autocrop.activities.main.fragments.flowfield

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.net.Uri
import android.text.SpannableStringBuilder
import android.view.View
import androidx.annotation.IdRes
import androidx.appcompat.widget.PopupMenu
import androidx.core.text.color
import androidx.core.text.italic
import androidx.core.view.children
import com.autocrop.activities.main.MainActivity
import com.autocrop.activities.main.MainActivityViewModel
import com.autocrop.activities.main.fragments.about.AboutFragment
import com.autocrop.activities.main.fragments.flowfield.views.MainActivityViewModelRetriever
import com.autocrop.global.BooleanPreferences
import com.autocrop.global.CropSavingPreferences
import com.autocrop.retriever.activity.ActivityRetriever
import com.autocrop.retriever.activity.ContextBasedActivityRetriever
import com.autocrop.retriever.viewmodel.ViewModelRetriever
import com.autocrop.uielements.ExtendedPopupMenu
import com.autocrop.uielements.makeIconsVisible
import com.autocrop.uielements.persistMenuAfterClick
import com.autocrop.utils.BlankFun
import com.autocrop.utilsandroid.getColorInt
import com.autocrop.utilsandroid.show
import com.autocrop.utilsandroid.snacky
import com.w2sv.autocrop.R

class FlowFieldPopupMenu(context: Context, anchor: View):
    ExtendedPopupMenu(context, anchor, R.menu.fragment_flowfield),
    ActivityRetriever<MainActivity> by ContextBasedActivityRetriever(context),
    ViewModelRetriever<MainActivityViewModel> by MainActivityViewModelRetriever(context) {

    init{
        setIconColor(context, R.color.magenta_bright)
        setCheckableItems(context)
        setItemOnClickListeners(
            mapOf(
                R.id.main_menu_item_change_save_destination_dir to ::pickCropSaveDestinationDir,
                R.id.main_menu_item_rate_the_app to ::goToPlayStoreListing,
                R.id.main_menu_item_about_the_app to ::invokeAboutFragment
            )
        )
        styleGroupDividers(
            context,
            arrayOf(
                R.id.main_menu_group_divider_examination,
                R.id.main_menu_group_divider_crop_saving,
                R.id.main_menu_group_divider_other
            )
        )
        menu.makeIconsVisible()
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

    /**
     * Sets check and [setOnMenuItemClickListener]
     */
    private fun setCheckableItems(context: Context) = mapOf(
        R.id.main_menu_item_auto_scroll to "autoScroll"
    )
        .forEach { (id, userPreferencesKey) ->
            with(menu.findItem(id)){
                val value = BooleanPreferences.getValue(userPreferencesKey)

                isChecked = value
                persistMenuAfterClick(context)
                setOnMenuItemClickListener {
                    BooleanPreferences[userPreferencesKey] = !value
                    isChecked = !isChecked

                    false
                }
            }
    }
}