package com.bunsenbrenner.screenshotboundremoval

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.ContentResolver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import kotlinx.android.synthetic.main.toolbar.*
import java.util.*
import kotlin.math.abs


const val N_SAVED_CROPS: String = "com.example.screenshotboundremoval.N_SAVED_CROPS"

private fun saveCroppedAndDeleteOriginal(imageUri: Uri,
                                         croppedImage: Bitmap,
                                         context: Context,
                                         cr: ContentResolver){
    imageUri.deleteUnderlyingResource(context)
    saveCroppedImage(
        cr,
        croppedImage,
        imageUri.getRealPath(context)
    )
}

class ExaminationActivity : FragmentActivity() {
    private lateinit var imageSlider: ViewPager
    private lateinit var sliderAdapter: ImageSliderAdapter
    companion object{
        var disableSavingButtons = false
    }

    override fun onStart() {
        super.onStart()
        hideSystemUI(window)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_NOSENSOR

        // set layout, retrieve layout elements
        setContentView(R.layout.activity_examination)
        val progressBar: ProgressBar = findViewById(R.id.indeterminateBar)
        val pageIndication: TextView = findViewById(R.id.page_indication)
        val titleTextView: TextView = findViewById(R.id.title_text_view)

        // if applicable display message informing about images which couldn't be cropped
        intent.getIntExtra(N_DISMISSED_IMAGES, 0).run{
            when (this){
                1 -> displayMessage(
                    "Couldn't find cropping bounds for 1 image",
                    this@ExaminationActivity
                )
                in 1..Int.MAX_VALUE -> displayMessage(
                    "Couldn't find cropping bounds for $this images",
                    this@ExaminationActivity
                )
            }
        }

        // initialize image slider
        imageSlider = findViewById(R.id.slide)
        imageSlider.apply{
            this.setPageTransformer(true,
                ZoomOutPageTransformer()
            )
            sliderAdapter =
                ImageSliderAdapter(
                    this@ExaminationActivity,
                    supportFragmentManager,
                    contentResolver,
                    imageSlider,
                    pageIndication,
                    titleTextView
                )
            this.adapter = sliderAdapter
        }

        // set toolbar button onClickListeners
        save_all_button.setOnClickListener{
            if (!disableSavingButtons){
                disableSavingButtons = true
                AsyncSaveAllOnClickExecutor(
                    progressBar,
                    sliderAdapter,
                    this,
                    contentResolver
                ).execute()
            }
        }

        dismiss_all_button.setOnClickListener{
            if (!disableSavingButtons){
                disableSavingButtons = true
                sliderAdapter.returnToMainActivity()
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        hideSystemUI(window)
    }

    /**
     * display saving result message on back button press
     */
    override fun onBackPressed() {
        sliderAdapter.returnToMainActivity()
    }

    private class AsyncSaveAllOnClickExecutor(val progressBar: ProgressBar,
                                              val sliderAdapter: ImageSliderAdapter,
                                              val context: Context,
                                              val contentResolver: ContentResolver): AsyncTask<Void, Void, Void?>() {
        override fun onPreExecute() {
            super.onPreExecute()
            progressBar.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg params: Void?): Void? {
            for (i in 0 until sliderAdapter.count){
                saveCroppedAndDeleteOriginal(
                    sliderAdapter.imageUris[i],
                    sliderAdapter.croppedImages[i],
                    context,
                    contentResolver
                )
                sliderAdapter.savedCrops += 1
            }
            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            progressBar.visibility = View.INVISIBLE
            sliderAdapter.returnToMainActivity()
        }
    }
}


/**
 *  class holding both cropped images and corresponding uris,
 *  defining sliding/image displaying behavior and inherent side effects such as
 *      the page indication updating
 */
class ImageSliderAdapter(private val context: Context,
                         private val fm: FragmentManager,
                         private val cr: ContentResolver,
                         private val imageSlider: ViewPager,
                         val pageIndication: TextView,
                         val titleTextView: TextView): PagerAdapter(){
    val croppedImages: MutableList<Bitmap> = ImageCash.values()
        .toMutableList()
    val imageUris: MutableList<Uri> = ImageCash.keys()
        .toMutableList().also { ImageCash.clear() }
    var savedCrops: Int = 0

    init {
        // change page indication text view on slide execution
        class PageChangeListener: ViewPager.SimpleOnPageChangeListener(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val displayPosition: Int = position + 1
                pageIndication.setText("$displayPosition/$count  ")
            }
        }
        imageSlider.addOnPageChangeListener(PageChangeListener())
        pageIndication.setText("1/$count  ")
    }

    fun returnToMainActivity(){
        ExaminationActivity.disableSavingButtons = false
        val intent = Intent(context, MainActivity::class.java).
            apply{this.putExtra(N_SAVED_CROPS, savedCrops)}
        startActivity(context, intent, null)
    }

    // -----------------
    // OVERRIDES
    // -----------------
    override fun getCount(): Int = croppedImages.size

    override fun getItemPosition(obj: Any): Int = POSITION_NONE

    override fun isViewFromObject(view: View, obj: Any): Boolean = view == obj

    inner class ViewPagerImageView(context: Context,
                             private val contentResolver: ContentResolver,
                             private val imageSlider: ViewPager,
                             private val position: Int,
                             private val imageSliderAdapter: ImageSliderAdapter,
                             private val container: ViewGroup,
                             private val fragmentManager: FragmentManager): ImageView(context){

        private var startX: Float = 0.toFloat()
        private var startY: Float = 0.toFloat()

        val CLICK_MANHATTEN_NORM_THRESHOLD: Int = 100

        override fun onTouchEvent(event: MotionEvent?): Boolean {

            fun isClick(startX: Float,
                        startY: Float,
                        endX: Float,
                        endY: Float): Boolean = (abs(startX - endX) + abs(startY - endY)) < CLICK_MANHATTEN_NORM_THRESHOLD

            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = event.x
                    startY = event.y
                }

                MotionEvent.ACTION_UP -> {

                    if (isClick(startX, startY, event.x, event.y) && !ExaminationActivity.disableSavingButtons)
                        ProcedureDialog(
                            context,
                            contentResolver,
                            imageSlider,
                            position,
                            imageSliderAdapter,
                            container
                        ).show(fragmentManager, "procedure")
                }
            }
            return true
        }
    }

    override fun instantiateItem(container: ViewGroup, position: Int): ImageView = ViewPagerImageView(
        context,
        cr,
        imageSlider,
        position,
        this,
        container,
        fm).apply {
            this.scaleType = ImageView.ScaleType.FIT_CENTER
            this.setImageBitmap(croppedImages[position])
            container.addView(this, position)
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {}
}

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
    private inner class SaveButtonOnClickListener: DialogInterface.OnClickListener{
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

    private inner class DismissButtonOnClickListener: DialogInterface.OnClickListener{
        override fun onClick(dialog: DialogInterface?, which: Int) {
            postButtonPress()
        }
    }
}
