package com.autocrop.activities.examination.imageslider

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.viewpager.widget.ViewPager
import com.autocrop.GlobalParameters
import com.autocrop.ops.saveImageAndDeleteScreenshotIfApplicable
import com.autocrop.utils.android.paddedMessage
import com.autocrop.utils.toInt


/**
 * Class accounting for procedure dialog message display on screen touch,
 * defining respective procedure effects
 */
class ProcedureDialog(
    private val activityContext: Context,
    private val imageSlider: ViewPager,
    private val position: Int,
    private val imageSliderAdapter: ImageSliderAdapter,
    private val container: ViewGroup
) : DialogFragment() {

    val imageUri: Uri = imageSliderAdapter.imageUris[position]
    val croppedImage: Bitmap = imageSliderAdapter.croppedImages[position].first

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(this.activity).run {
            this
                .setTitle(
                    paddedMessage(
                        *listOf(
                            listOf("Save crop?"),
                            listOf("Save crop and delete", "original screenshot?")
                        )[GlobalParameters.deleteInputScreenshots.toInt()].toTypedArray()
                    )
                )
                .setNegativeButton(
                    "Yes",
                    SaveButtonOnClickListener()
                )  // vice-versa setting required for making yes appear first
                .setPositiveButton("No", DismissButtonOnClickListener())

            this.create()
        }
    }

    // ---------------OnClickListeners---------------------

    private inner class SaveButtonOnClickListener :
        DialogInterface.OnClickListener {
        override fun onClick(dialog: DialogInterface?, which: Int) {
            saveImageAndDeleteScreenshotIfApplicable(
                imageUri,
                croppedImage,
                activityContext
            )
            imageSliderAdapter.savedCrops += 1
            postButtonPress()
        }
    }

    private inner class DismissButtonOnClickListener :
        DialogInterface.OnClickListener {
        override fun onClick(dialog: DialogInterface?, which: Int) {
            postButtonPress()
        }
    }

    /**
     * remove current image from view pager
     * move to new view
     * return to main activity in case of previously handled image being last one in view
     */
    private fun postButtonPress() {
        imageSliderAdapter.apply {
            if (this.count == 1)
                this.returnToMainActivity()
        }

        imageSlider.apply {
            this.currentItem = 0
            this.removeAllViews()
        }

        imageSliderAdapter.apply {
            this.imageUris.removeAt(position).also { this.croppedImages.removeAt(position) }
            this.notifyDataSetChanged()
        }

        val newPosition: Int = if (position != imageSliderAdapter.count) position else position - 1

        for (i in 0..newPosition)
            imageSliderAdapter.instantiateItem(container, i)

        imageSlider.setCurrentItem(newPosition, true)

        val pages: Int = imageSliderAdapter.count

        imageSliderAdapter.run {
            if (this.count > 0) {
                textViews.setRetentionPercentageText(imageSliderAdapter.croppedImages[newPosition].second)
                textViews.setPageIndicationText(newPosition + 1, pages)
            } else {
                this.textViews.retentionPercentage.visibility = View.INVISIBLE
                textViews.setPageIndicationText(69, 420)
                this.textViews.appTitle.visibility = View.VISIBLE
            }
        }
    }
}