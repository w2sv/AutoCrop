package com.example.screenshotboundremoval

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.view.size
import androidx.fragment.app.FragmentActivity
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import java.io.File


const val DELETION_RESULT: String = "com.example.screenshotboundremoval.DELETION_RESULT"

class ProcedureActivity : FragmentActivity() {
    private lateinit var mPager: ViewPager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.image_slide)
        mPager = findViewById(R.id.slide)
        mPager.adapter = ImageSliderAdaper(this)

        /*
        imageView.setOnClickListener(View.OnClickListener() {
            // query procedure
            openDialog(originalImageUri, savedImageUri)
        })*/
    }
}

class ImageSliderAdaper(private val context: Context): PagerAdapter(){

    private val croppedImages: List<Bitmap> = ImageCash.cash.values.toList()

    override fun getCount(): Int = croppedImages.size
    override fun isViewFromObject(view: View, obj: Any): Boolean = view == obj

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val imageView = ImageView(context)
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        imageView.setImageBitmap(croppedImages[position])
        /*container.addView(imageView, position)

        imageView.setOnClickListener(View.OnClickListener(){

        })*/
        return imageView
    }

    /*private fun openDialog(originalImageUri: Uri, savedImageUri: Uri) {
        val dialog = ProcedureDialog(originalImageUri, savedImageUri, this)
        dialog.show(supportFragmentManager, "procedure")
    }*/

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        // TODO: debug
    }
}

class ProcedureDialog(val originalImageUri: Uri, val savedImageUri: Uri, val activityContext: Context) : AppCompatDialogFragment(){

    companion object{
        const val DELETED_ORIGINAL_IMAGE = 1337
    }

    private fun getRealPathFromURI(context: Context, contentUri: Uri): String? {
        var cursor: Cursor? = null
        return try {
            val images: Array<String> = arrayOf(MediaStore.Images.Media.DATA)
            cursor = context.contentResolver.query(contentUri, images, null, null, null)

            val columnIndex = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            cursor.getString(columnIndex)
        } catch (e: Exception) {
            println( "getRealPathFromURI Exception : $e")
            ""
        } finally {
            cursor?.close()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(this.activity)
        builder
            .setTitle("Save and delete original screenshot?")
            .setPositiveButton("Yes", DiscardButtonOnClickListener())
            .setNegativeButton("No, dismiss cropped image", KeepButtonOnClickListener())
        return builder.create()
    }

    private fun restartMainActivity(resultCode: Int?){
        val intent = Intent(context, MainActivity::class.java).apply{
            this.putExtra(DELETION_RESULT, resultCode)
        }
        startActivity(intent)
    }

    private fun deleteImage(uri: Uri){
        val deleteFile = File(getRealPathFromURI(activityContext, uri)!!)

        if (deleteFile.exists()){
            deleteFile.canonicalFile.delete()

            if(deleteFile.exists())
                activityContext.deleteFile(deleteFile.name)

            activityContext.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(File(getRealPathFromURI(activityContext, uri)!!))))
        }
    }

    // ---------------
    // BUTTON CLASSES
    // ---------------

    private inner class KeepButtonOnClickListener: DialogInterface.OnClickListener{
        override fun onClick(dialog: DialogInterface?, which: Int) = restartMainActivity(-1)
    }

    private inner class DiscardButtonOnClickListener: DialogInterface.OnClickListener{
        override fun onClick(dialog: DialogInterface?, which: Int) {
            deleteImage(savedImageUri)
            restartMainActivity(-1)
        }
    }

    private inner class OriginalImageDeletionOnClickListener: DialogInterface.OnClickListener{
        override fun onClick(dialog: DialogInterface?, which: Int) {
            deleteImage(originalImageUri)
            restartMainActivity(DELETED_ORIGINAL_IMAGE)
        }
    }
}
