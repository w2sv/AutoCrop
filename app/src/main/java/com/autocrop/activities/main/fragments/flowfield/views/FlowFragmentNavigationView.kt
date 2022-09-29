package com.autocrop.activities.main.fragments.flowfield.views

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.AttributeSet
import android.widget.Switch
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.autocrop.activities.main.MainActivity
import com.autocrop.activities.main.MainActivityViewModel
import com.autocrop.activities.main.fragments.about.AboutFragment
import com.autocrop.preferences.BooleanPreferences
import com.autocrop.preferences.UriPreferences
import com.autocrop.retriever.activity.ActivityRetriever
import com.autocrop.retriever.activity.ContextBasedActivityRetriever
import com.autocrop.retriever.viewmodel.ViewModelRetriever
import com.autocrop.ui.elements.view.ifNotInEditMode
import com.autocrop.utils.android.IMAGE_MIME_TYPE
import com.autocrop.utils.android.extensions.show
import com.autocrop.utils.android.extensions.snacky
import com.google.android.material.navigation.NavigationView
import com.w2sv.autocrop.R

class FlowFragmentNavigationView(context: Context, attributeSet: AttributeSet):
    NavigationView(context, attributeSet),
    ActivityRetriever<MainActivity> by ContextBasedActivityRetriever(context),
    ViewModelRetriever<MainActivityViewModel> by MainActivityViewModelRetriever(context){

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        ifNotInEditMode {
            with(menu.findItem(R.id.main_menu_item_share_crops)){
                if (sharedViewModel.savedCropUris == null)
                    icon?.alpha = 130
                else
                    isEnabled = true
            }

            menu.findItem(R.id.main_menu_item_auto_scroll).actionView = Switch(context).apply {
                isChecked = BooleanPreferences.autoScroll
                setOnCheckedChangeListener { _, isChecked ->
                    BooleanPreferences.autoScroll = isChecked
                }
            }

            setNavigationItemSelectedListener {
                when (it.itemId){
                    R.id.main_menu_item_share_crops -> launchCropSharingIntent()
                    R.id.main_menu_item_change_save_destination_dir -> pickSaveDestinationDir()
                    R.id.main_menu_item_about_the_app -> invokeAboutFragment()
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
                    putParcelableArrayListExtra(Intent.EXTRA_STREAM, sharedViewModel.savedCropUris)
                    type = IMAGE_MIME_TYPE
                },
                null
            )
        )
    }

    private fun pickSaveDestinationDir(){
        sharedViewModel.pickSaveDestinationDir.launch(UriPreferences.treeUri)
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
