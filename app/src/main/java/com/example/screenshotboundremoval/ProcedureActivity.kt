package com.example.screenshotboundremoval

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
import android.widget.ImageView
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager


const val DELETION_RESULT: String = "com.example.screenshotboundremoval.DELETION_RESULT"

class ProcedureActivity : FragmentActivity() {
    private lateinit var mPager: ViewPager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.image_slide)
        mPager = findViewById(R.id.slide)
        mPager.adapter = ImageSliderAdapter(this, supportFragmentManager, contentResolver)
    }

    /*
    * clear image cash on back press
    * */
    override fun onBackPressed() {
        ImageCash.clear()
        super.onBackPressed()
    }
}

class ImageSliderAdapter(private val context: Context, private val fm: FragmentManager, private val cr: ContentResolver): PagerAdapter(){

    private val croppedImages: List<Bitmap> = ImageCash.values().toList()
    private val imageUris: List<Uri> = ImageCash.keys().toList()

    override fun getCount(): Int = croppedImages.size
    override fun isViewFromObject(view: View, obj: Any): Boolean = view == obj

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val imageView = ImageView(context)
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        imageView.setImageBitmap(croppedImages[position])
        container.addView(imageView, position)

        imageView.setOnClickListener(View.OnClickListener(){
            // open dialog
            val dialog = ProcedureDialog(context, cr, imageUris[position], croppedImages[position])
            dialog.show(fm, "procedure")
        })
        return imageView
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        // TODO: debug
    }

    /*private fun restartMainActivity(resultCode: Int?){
        val intent = Intent(context, MainActivity::class.java).apply{
            this.putExtra(DELETION_RESULT, resultCode)
        }
        startActivity(intent)
    }*/
}

class ProcedureDialog(private val activityContext: Context,
                      private val cr: ContentResolver,
                      private val originalImageUri: Uri,
                      private val croppedImage: Bitmap) : AppCompatDialogFragment(){

    companion object{
        const val DELETED_ORIGINAL_IMAGE = 1337
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(this.activity)
        builder
            .setTitle("Save and delete original screenshot?")
            .setNegativeButton("Yes", SaveButtonOnClickListener())  // vice-versa in order to make yes appear first
            .setPositiveButton("No, dismiss cropped image", DismissButtonOnClickListener())
        return builder.create()
    }

    // ---------------
    // BUTTON CLASSES
    // ---------------
    private inner class SaveButtonOnClickListener: DialogInterface.OnClickListener{
        override fun onClick(dialog: DialogInterface?, which: Int){
            originalImageUri.deleteUnderlyingRessource(activityContext)
            saveCroppedImage(cr, croppedImage, originalImageUri.getRealPath(activityContext))
        }
    }

    private inner class DismissButtonOnClickListener: DialogInterface.OnClickListener{
        override fun onClick(dialog: DialogInterface?, which: Int) {
        }
    }
}
