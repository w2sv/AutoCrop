package com.bunsenbrenner.screenshotboundremoval.activities.examination

import android.app.AlertDialog
import android.app.Dialog
import android.content.ContentResolver
import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.viewpager.widget.ViewPager

/**
 * class accounting for procedure dialog message display on screen touch,
 * defining respective procedure effects
 */
class ProcedureDialog(private val activityContext: Context,
                      private val cr: ContentResolver,
                      private val imageSlider: ViewPager,
                      private val position: Int,
                      private val imageSliderAdapter: ImageSliderAdapter,
                      private val container: ViewGroup) : DialogFragment(){

    val imageUri: Uri = imageSliderAdapter.imageUris[position]
    val croppedImage: Bitmap = imageSliderAdapter.croppedImages[position]

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(this.activity)
        builder
            .setTitle("Save and delete original screenshot?")
            .setNegativeButton("Yes", SaveButtonOnClickListener())  // vice-versa setting required for making yes appear first
            .setPositiveButton("No", DismissButtonOnClickListener())
        return builder.create()
    }

    /**
     * remove current image from view pager
     * move to new view
     * return to main activity in case of previously handled image being last one in view
     */
    private fun postButtonPress(){
        imageSliderAdapter.apply {
            if (this.count == 1)
                this.returnToMainActivity()
        }

        imageSlider.apply{
            this.currentItem = 0
            this.removeAllViews()
        }

        imageSliderAdapter.apply{
            this.imageUris.removeAt(position).also { this.croppedImages.removeAt(position) }
            this.notifyDataSetChanged()
        }

        val newPosition: Int = if (position != imageSliderAdapter.count) position else position -1

        for (i in 0..newPosition)
            imageSliderAdapter.instantiateItem(container, i)

        imageSlider.setCurrentItem(newPosition, true)

        val pages: Int = imageSliderAdapter.count
        val newDisplayPosition: Int = newPosition + 1

        imageSliderAdapter.run {
            if (this.count == 0){
                this.titleTextView.visibility = View.VISIBLE
                this.pageIndication.setText("69/420 ")
            }
            else
                this.pageIndication.setText("$newDisplayPosition/$pages  ")
        }

    }

    // ---------------
    // ON CLICK LISTENERS
    // ---------------
    private inner class SaveButtonOnClickListener:
        DialogInterface.OnClickListener {
        override fun onClick(dialog: DialogInterface?, which: Int){
            saveCroppedAndDeleteOriginal(
                imageUri,
                croppedImage,
                activityContext,
                cr
            )
            imageSliderAdapter.savedCrops += 1
            postButtonPress()
        }
    }

    private inner class DismissButtonOnClickListener:
        DialogInterface.OnClickListener {
        override fun onClick(dialog: DialogInterface?, which: Int) {
            postButtonPress()
        }
    }
}