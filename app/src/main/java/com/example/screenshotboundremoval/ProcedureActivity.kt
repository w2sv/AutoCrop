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
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager


const val SAVED_CROPS: String = "com.example.screenshotboundremoval.DELETION_RESULT"

class ProcedureActivity : FragmentActivity() {
    private lateinit var mPager: ViewPager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.image_slide)
        mPager = findViewById(R.id.slide)
        mPager.setPageTransformer(true, ZoomOutPageTransformer())
        mPager.adapter = ImageSliderAdapter(this, supportFragmentManager, contentResolver, mPager)
    }
}

class ImageSliderAdapter(private val context: Context,
                         private val fm: FragmentManager,
                         private val cr: ContentResolver,
                         private val mPager: ViewPager): PagerAdapter(){

    val croppedImages: MutableList<Bitmap> = ImageCash.values().toMutableList()
    val imageUris: MutableList<Uri> = ImageCash.keys().toMutableList().also { ImageCash.clear() }
    var savedCrops: Int = 0

    override fun getCount(): Int = croppedImages.size
    override fun isViewFromObject(view: View, obj: Any): Boolean = view == obj

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val imageView = ImageView(context)
        imageView.scaleType = ImageView.ScaleType.FIT_CENTER
        imageView.setImageBitmap(croppedImages[position])
        container.addView(imageView, position)

        imageView.setOnClickListener(View.OnClickListener(){
            // open dialog
            val dialog = ProcedureDialog(context, cr, mPager, position, this, container, imageView)
            dialog.show(fm, "procedure")
        })
        return imageView
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        // TODO: debug
    }

    override fun getItemPosition(obj: Any): Int {
        return POSITION_NONE
    }
}

class ProcedureDialog(private val activityContext: Context,
                      private val cr: ContentResolver,
                      private val mPager: ViewPager,
                      private val position: Int,
                      private val imageSliderAdapter: ImageSliderAdapter,
                      private val container: ViewGroup,
                      private val imageView: ImageView) : AppCompatDialogFragment(){

    val imageUri: Uri = imageSliderAdapter.imageUris[position]
    val croppedImage: Bitmap = imageSliderAdapter.croppedImages[position]

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(this.activity)
        builder
            .setTitle("Save and delete original screenshot?")
            .setNegativeButton("Yes", SaveButtonOnClickListener())  // vice-versa required for making yes appear first
            .setPositiveButton("No, dismiss cropped image", DismissButtonOnClickListener())
        return builder.create()
    }

    private fun postButtonPress(){
        if (imageSliderAdapter.count == 1){
            // restart main activity
            val intent = Intent(context, MainActivity::class.java).
                apply{this.putExtra(SAVED_CROPS, imageSliderAdapter.savedCrops)}
            startActivity(intent)
        }

        mPager.currentItem = 0
        mPager.removeAllViews()

        imageSliderAdapter.imageUris.removeAt(position)
        imageSliderAdapter.croppedImages.removeAt(position)

        imageSliderAdapter.notifyDataSetChanged()
        mPager.setCurrentItem(if (position != imageSliderAdapter.count) position else position -1, true)
    }

    // ---------------
    // BUTTON CLASSES
    // ---------------
    private inner class SaveButtonOnClickListener: DialogInterface.OnClickListener{
        override fun onClick(dialog: DialogInterface?, which: Int){
            imageUri.deleteUnderlyingRessource(activityContext)
            saveCroppedImage(cr, croppedImage, imageUri.getRealPath(activityContext))
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
