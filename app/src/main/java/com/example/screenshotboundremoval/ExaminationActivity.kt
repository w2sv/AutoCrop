package com.example.screenshotboundremoval

import android.app.AlertDialog
import android.app.Dialog
import android.content.ContentResolver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.content.ContextCompat.startActivity
import androidx.core.view.isInvisible
import androidx.fragment.app.FragmentManager
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import kotlinx.android.synthetic.main.toolbar.*


const val SAVED_CROPS: String = "com.example.screenshotboundremoval.DELETION_RESULT"

private fun saveCroppedAndDeleteOriginal(imageUri: Uri,
                                         croppedImage: Bitmap,
                                         context: Context,
                                         cr: ContentResolver){
    imageUri.deleteUnderlyingRessource(context)
    saveCroppedImage(cr, croppedImage, imageUri.getRealPath(context))
}

class ProcedureActivity : AppCompatActivity() {
    private lateinit var mPager: ViewPager
    private lateinit var sliderAdapter: ImageSliderAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val nDismissedImages: Int = intent.getIntExtra(N_DISMISSED_IMAGES, 0)
        if (nDismissedImages != 0){
            when (nDismissedImages){
                1 -> displayMessage("Couldn't find cropping bounds for $nDismissedImages image", this)
                in 1..Int.MAX_VALUE -> displayMessage("Couldn't find cropping bounds for $nDismissedImages images", this)
            }
        }

        setContentView(R.layout.activity_examination)
        val progressBar: ProgressBar = findViewById(R.id.indeterminateBar)
        progressBar.visibility = View.GONE

        val pageIndication: TextView = findViewById<TextView>(R.id.page_indication)

        mPager = findViewById(R.id.slide)
        mPager.setPageTransformer(true, ZoomOutPageTransformer())
        sliderAdapter = ImageSliderAdapter(this, supportFragmentManager, contentResolver, mPager, pageIndication)
        mPager.adapter = sliderAdapter

        save_all_button.setOnClickListener{
            progressBar.visibility = View.VISIBLE
            for (i in 0 until sliderAdapter.count){
                saveCroppedAndDeleteOriginal(sliderAdapter.imageUris[i], sliderAdapter.croppedImages[i],this, contentResolver)
                sliderAdapter.savedCrops += 1
            }
            sliderAdapter.returnToMainActivity()
        }

        dismiss_all_button.setOnClickListener{
            sliderAdapter.returnToMainActivity()
        }
    }

    override fun onBackPressed() {
        sliderAdapter.returnToMainActivity()
    }
}

class ImageSliderAdapter(private val context: Context,
                         private val fm: FragmentManager,
                         private val cr: ContentResolver,
                         private val mPager: ViewPager,
                         val pageIndication: TextView): PagerAdapter(){

    val croppedImages: MutableList<Bitmap> = ImageCash.values().toMutableList()
    val imageUris: MutableList<Uri> = ImageCash.keys().toMutableList().also { ImageCash.clear() }
    var savedCrops: Int = 0

    init {
        class PageChangeListener: ViewPager.SimpleOnPageChangeListener(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val displayPosition: Int = position + 1
                pageIndication.setText("$displayPosition/$count  ")
            }
        }
        mPager.addOnPageChangeListener(PageChangeListener())
        pageIndication.setText("1/$count  ")
    }

    fun returnToMainActivity(){
        val intent = Intent(context, MainActivity::class.java).
            apply{this.putExtra(SAVED_CROPS, savedCrops)}
        startActivity(context, intent, null)
    }

    // -----------------
    // overrides
    // -----------------
    override fun getCount(): Int = croppedImages.size
    override fun isViewFromObject(view: View, obj: Any): Boolean = view == obj

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val imageView = ImageView(context)
        imageView.scaleType = ImageView.ScaleType.FIT_CENTER
        imageView.setImageBitmap(croppedImages[position])
        container.addView(imageView, position)

        imageView.setOnClickListener{
            ProcedureDialog(context, cr, mPager, position, this).show(fm, "procedure")
        }
        return imageView
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        // container.removeViewAt(position)
    }

    override fun getItemPosition(obj: Any): Int {
        return POSITION_NONE
    }
}

class ProcedureDialog(private val activityContext: Context,
                      private val cr: ContentResolver,
                      private val mPager: ViewPager,
                      private val position: Int,
                      private val imageSliderAdapter: ImageSliderAdapter) : AppCompatDialogFragment(){

    val imageUri: Uri = imageSliderAdapter.imageUris[position]
    val croppedImage: Bitmap = imageSliderAdapter.croppedImages[position]

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(this.activity)
        builder
            .setTitle("Save and delete original screenshot?")
            .setNegativeButton("Yes", SaveButtonOnClickListener())  // vice-versa required for making yes appear first
            .setPositiveButton("No", DismissButtonOnClickListener())
        return builder.create()
    }

    /*
     * remove current image from view pager
     * move to new view
     * return to main activity in case of previously handled image being last one in view
     */
    private fun postButtonPress(){
        if (imageSliderAdapter.count == 1){
            imageSliderAdapter.returnToMainActivity()
        }

        mPager.currentItem = 0
        mPager.removeAllViews()

        imageSliderAdapter.imageUris.removeAt(position)
        imageSliderAdapter.croppedImages.removeAt(position)

        imageSliderAdapter.notifyDataSetChanged()
        // mPager.setCurrentItem(if (position != imageSliderAdapter.count) position else position -1, true)
        mPager.setCurrentItem(0, true)

        val pages: Int = imageSliderAdapter.count
        imageSliderAdapter.pageIndication.setText(if (imageSliderAdapter.count > 0) "1/$pages  " else "0/0 ")
    }

    // ---------------
    // BUTTON CLASSES
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
