package com.example.screenshotboundremoval

import android.app.AlertDialog
import android.app.Dialog
import android.content.ContentResolver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.FragmentManager
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import kotlinx.android.synthetic.main.toolbar.*


const val N_SAVED_CROPS: String = "com.example.screenshotboundremoval.N_SAVED_CROPS"

private class SaveAllOnClickExecuter(val progressBar: ProgressBar,
                                     val sliderAdapter: ImageSliderAdapter,
                                     val context: Context,
                                     val contentResolver: ContentResolver): AsyncTask<Void, Void, Void?>() {
    override fun onPreExecute() {
        super.onPreExecute()
        progressBar.visibility = View.VISIBLE
    }

    override fun doInBackground(vararg params: Void?): Void? {
        for (i in 0 until sliderAdapter.count){
            saveCroppedAndDeleteOriginal(sliderAdapter.imageUris[i], sliderAdapter.croppedImages[i], context, contentResolver)
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


private fun saveCroppedAndDeleteOriginal(imageUri: Uri,
                                         croppedImage: Bitmap,
                                         context: Context,
                                         cr: ContentResolver){
    // imageUri.deleteUnderlyingRessource(context) !
    saveCroppedImage(cr, croppedImage, imageUri.getRealPath(context))
}
class ProcedureActivity : AppCompatActivity() {
    private lateinit var imageSlider: ViewPager
    private lateinit var sliderAdapter: ImageSliderAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // set layout, retrieve layout elements
        setContentView(R.layout.activity_examination)
        val progressBar: ProgressBar = findViewById(R.id.indeterminateBar)
        val pageIndication: TextView = findViewById(R.id.page_indication)

        // if applicable display message informing about images which couldn't be cropped
        intent.getIntExtra(N_DISMISSED_IMAGES, 0).let{
            when (it){
                1 -> displayMessage("Couldn't find cropping bounds for $it image", this)
                in 1..Int.MAX_VALUE -> displayMessage("Couldn't find cropping bounds for $this images", this)
            }
        }

        // initialize image slider
        imageSlider = findViewById(R.id.slide)
        imageSlider.apply{
            this.setPageTransformer(true, ZoomOutPageTransformer())
            sliderAdapter = ImageSliderAdapter(this@ProcedureActivity, supportFragmentManager, contentResolver, imageSlider, pageIndication)
            this.adapter = sliderAdapter
        }


        // set toolbar button onClickListeners
        save_all_button.setOnClickListener{
            SaveAllOnClickExecuter(progressBar, sliderAdapter, this, contentResolver).execute()
        }

        dismiss_all_button.setOnClickListener{
            sliderAdapter.returnToMainActivity()
        }
    }

    /**
     * display saving result message on back button press
     */
    override fun onBackPressed() {
        sliderAdapter.returnToMainActivity()
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
                         val pageIndication: TextView): PagerAdapter(){
    val croppedImages: MutableList<Bitmap> = ImageCash.values().toMutableList()
    val imageUris: MutableList<Uri> = ImageCash.keys().toMutableList().also { ImageCash.clear() }
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

    override fun instantiateItem(container: ViewGroup, position: Int): ImageView = ImageView(context).apply{
        this.scaleType = ImageView.ScaleType.FIT_CENTER
        this.setImageBitmap(croppedImages[position])
        container.addView(this, position)

        this.setOnClickListener{
            ProcedureDialog(context, cr, imageSlider, position, this@ImageSliderAdapter).show(fm, "procedure")
        }
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        // container.removeViewAt(position)
    }
}

/**
 * class accounting for procedure dialog message display on screen touch,
 * defining respective procedure effects
 */
class ProcedureDialog(private val activityContext: Context,
                      private val cr: ContentResolver,
                      private val imageSlider: ViewPager,
                      private val position: Int,
                      private val imageSliderAdapter: ImageSliderAdapter) : AppCompatDialogFragment(){

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
            this.imageUris.removeAt(position)
            this.croppedImages.removeAt(position)
            this.notifyDataSetChanged()
        }

        // mPager.setCurrentItem(if (position != imageSliderAdapter.count) position else position -1, true)
        imageSlider.setCurrentItem(0, true)

        val pages: Int = imageSliderAdapter.count
        imageSliderAdapter.pageIndication.setText(if (imageSliderAdapter.count > 0) "1/$pages  " else "0/0 ")
    }

    // ---------------
    // ON CLICK LISTENERS
    // ---------------
    private inner class SaveButtonOnClickListener: DialogInterface.OnClickListener{
        override fun onClick(dialog: DialogInterface?, which: Int){
            saveCroppedAndDeleteOriginal(imageUri, croppedImage, activityContext, cr)
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
