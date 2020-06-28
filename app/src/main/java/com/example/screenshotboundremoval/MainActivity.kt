package com.example.screenshotboundremoval

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.widget.FrameLayout
import androidx.fragment.app.FragmentActivity
import kotlinx.android.synthetic.main.activity_main.*
import processing.android.PFragment


// TODO: deletion of viewpager items
//  abort google images cropping error

// TODO: consecutive elaboration: watermark removal by means of vae implemented in tensorflow lite
//  possibly orientation change enabling

const val N_DISMISSED_IMAGES = "com.example.screenshotboundremoval.N_DISMISSED_IMAGES"


class MainActivity : FragmentActivity() {

    companion object{
        private const val IMAGE_PICK_CODE = 69
        private const val READ_PERMISSION_CODE = 420
        private const val WRITE_PERMISSION_CODE = 47

        var pixelField: PixelField? = null
    }

    private var nRequiredPermissions: Int = -1
    private val permission2Code: Map<String, Int> = mapOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE to WRITE_PERMISSION_CODE,
        Manifest.permission.READ_EXTERNAL_STORAGE to READ_PERMISSION_CODE
    )

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //initialize PixelField on first creation/redraw and bind to PFragment anew on activity restart
        if (pixelField == null){
            val dm = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(dm)
            pixelField = PixelField(dm.widthPixels, dm.heightPixels)
        }
        else
            pixelField!!.redraw()

        val fragment = PFragment(pixelField)
        val frameLayout = findViewById<FrameLayout>(R.id.canvas_container)
        fragment.setView(frameLayout, this)

        // display saving result if present
        intent.getIntExtra(N_SAVED_CROPS, -1).run{
            when(this){
                0 -> displayMessage("Didn't save anything", this@MainActivity)
                1 -> displayMessage("Saved 1 cropped image", this@MainActivity)
                in 1..Int.MAX_VALUE -> displayMessage("Saved $this cropped images", this@MainActivity)
            }
        }

        // set image selection button onClickListener
        image_selection_button.setOnClickListener {
            requestActivityPermissions()

            if (nRequiredPermissions == 0)
                pickImageFromGallery()
        }
    }

    /**
     * enable exiting of app on back press
     */
    override fun onBackPressed() {
        finishAffinity()
    }

    // ----------------
    // PERMISSION QUERY
    // ----------------
    private fun requestActivityPermissions(){
        nRequiredPermissions = 0

        fun checkPermission(permission: String){
            if (checkSelfPermission(permission) == PackageManager.PERMISSION_DENIED){
                nRequiredPermissions ++
                requestPermissions(arrayOf(permission), permission2Code[permission]!!)
            }
        }

        checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        fun permissionRequestResultHandling(grantResults: IntArray, requestDescription: String){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_DENIED)
                displayMessage("You need to permit file $requestDescription in order\n for the app to work.", this)
            else
                nRequiredPermissions --
        }

        when (requestCode) {
            READ_PERMISSION_CODE -> permissionRequestResultHandling(grantResults, "reading")
            WRITE_PERMISSION_CODE -> permissionRequestResultHandling(grantResults, "writing")
        }

        if (nRequiredPermissions == 0)
            return pickImageFromGallery()
    }

    // ----------------
    // IMAGE SELECTION
    // ----------------
    private fun pickImageFromGallery() {
        Intent(Intent.ACTION_PICK).run{
            this.type = "image/*"
            this.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            startActivityForResult(this, IMAGE_PICK_CODE)
        }
    }

    // ----------------
    // CROPPING
    // ----------------
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK && requestCode == IMAGE_PICK_CODE){
            var nDismissedImages = 0
            val itemCount: Int = data?.clipData?.itemCount!!

            for (i in 0 until itemCount) {

                // retrieve uri and resolve into bitmap
                val imageUri: Uri = data.clipData?.getItemAt(i)?.uri!!
                val image: Bitmap? = BitmapFactory.decodeStream(contentResolver.openInputStream(imageUri))

                // crop image
                val croppedImage: Bitmap? = Cropper(image!!).getCroppedImage()

                // bind uri and image to ImageCash in case of valid crop, else increment nDismissedImages
                if (croppedImage != null)
                    ImageCash[imageUri] = croppedImage
                else
                    nDismissedImages += 1
            }

            // start ExaminationActivity in case of at least 1 successful crop, otherwise return to image selection screen
            if (nDismissedImages != itemCount)
                startExaminationActivity(nDismissedImages)
            else
                allImagesDismissedOutput(itemCount != 1)
        }
    }

    // --------------
    // FOLLOW-UP ACTIVITIES
    // --------------
    private fun startExaminationActivity(dismissedCrops: Int){
        startActivity(Intent(this, ExaminationActivity::class.java).
            putExtra(N_DISMISSED_IMAGES, dismissedCrops)
        )
    }

    private fun allImagesDismissedOutput(attemptedForMultipleImages: Boolean){
        when(attemptedForMultipleImages){
            true -> displayMessage("Couldn't find cropping bounds for \n any of the selected images", this)
            false -> displayMessage("Couldn't find cropping bounds for selected image", this)
        }
    }
}