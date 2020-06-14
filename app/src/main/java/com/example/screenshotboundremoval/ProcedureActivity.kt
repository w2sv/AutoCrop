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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import java.io.File


const val DELETION_RESULT: String = "com.example.screenshotboundremoval.DELETION_RESULT"


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
            .setTitle("How would you like to proceed?")
            .setPositiveButton("Discard", DiscardButtonOnClickListener())
            .setNegativeButton("Keep", KeepButtonOnClickListener())
            .setNeutralButton("Keep and delete original", OriginalImageDeletionOnClickListener())

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


class ProcedureActivity : FragmentActivity() {
    private lateinit var mPager: ViewPager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val oldUris: ArrayList<Uri> = intent.getParcelableArrayListExtra<Uri>(OLD_URIS)!!
        val croppedUris: ArrayList<Uri> = intent.getParcelableArrayListExtra<Uri>(CROPPED_URIS)!!

        setContentView(R.layout.image_slide)
        mPager = findViewById(R.id.slide)
        mPager.adapter = ImageSliderAdaper(this, oldUris, croppedUris)

        /*
        // display cropped image
        setContentView(R.layout.activity_image_slider)  // crashing when procedure activity layout seized
        val imageView: ImageView = findViewById(R.id.image_view)
        imageView.setImageBitmap(reloadedImage)

        imageView.setOnClickListener(View.OnClickListener() {
            // query procedure
            openDialog(originalImageUri, savedImageUri)
        })*/
    }

    /*private fun openDialog(originalImageUri: Uri, savedImageUri: Uri) {
        val dialog = ProcedureDialog(originalImageUri, savedImageUri, this)
        dialog.show(supportFragmentManager, "procedure")
    }*/
}

class ImageSliderAdaper(private val context: Context,
                        private val oldUris: ArrayList<Uri>,
                        private val croppedUris: ArrayList<Uri>): PagerAdapter(){

    private fun loadBitmap(uri: Uri): Bitmap = BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri))
    private val bitmaps: List<Bitmap> = croppedUris.map { loadBitmap(it)}.also { println("loaded bitmaps") }

    override fun getCount(): Int = oldUris.size
    override fun isViewFromObject(view: View, obj: Any): Boolean = view == obj

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val imageView = ImageView(context)
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        imageView.setImageBitmap(bitmaps[position])
        container.addView(imageView, position)
        return imageView
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        container.removeViewAt(position)
    }
}
