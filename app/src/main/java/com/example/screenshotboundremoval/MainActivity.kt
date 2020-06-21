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
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import kotlinx.android.synthetic.main.activity_main.*
import processing.android.PFragment


// TODO: dynamic canvas size setting, maybe welcome screen, deletion of viewpager items, dismiss all button,
//  non 0-resetting item switch, original screenshot deletion remark on first usage, item centering improvement

const val N_DISMISSED_IMAGES = "com.example.screenshotboundremoval.N_DISMISSED_IMAGES"


class MainActivity : FragmentActivity() {

    private companion object{
        private const val IMAGE_PICK_CODE = 69
        private const val READ_PERMISSION_CODE = 420
        private const val WRITE_PERMISSION_CODE = 47

        private var pixelField: PixelField? = null
    }

    private var nRequiredPermissions: Int = 0
    private val permission2Code: Map<String, Int> = mapOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE to WRITE_PERMISSION_CODE,
        Manifest.permission.READ_EXTERNAL_STORAGE to READ_PERMISSION_CODE
    )

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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

        intent.getIntExtra(SAVED_CROPS, -1).let{
            when(it){
                0 -> displayMessage("Didn't save anything", this)
                1 -> displayMessage("Saved 1 cropped image", this)
                in 1..Int.MAX_VALUE -> displayMessage("Saved $it cropped images", this)
            }
        }

        image_selection_button.setOnClickListener {
            requestActivityPermissions()

            if (nRequiredPermissions == 0)
                pickImageFromGallery()
        }
    }

    override fun onBackPressed() {
        finishAffinity()
    }

    // ----------------
    // PERMISSION QUERY
    // ----------------
    private fun checkPermission(permission: String){
        if (checkSelfPermission(permission) == PackageManager.PERMISSION_DENIED){
            requestPermissions(arrayOf(permission), permission2Code[permission]!!)
            nRequiredPermissions ++
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray) {

        when (requestCode) {
            READ_PERMISSION_CODE -> permissionRequestResultHandling(grantResults, "Read")
            WRITE_PERMISSION_CODE -> permissionRequestResultHandling(grantResults, "Write")
        }
    }

    private fun requestActivityPermissions(){
        checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    private fun permissionRequestResultHandling(grantResults: IntArray, requestDescription: String){
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_DENIED)
            Toast.makeText(this, "$requestDescription permission denied", Toast.LENGTH_SHORT).show()
        else
            nRequiredPermissions --
    }

    // ----------------
    // IMAGE SELECTION
    // ----------------
    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    // ----------------
    // SAVING
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
                if (croppedImage != null)
                    ImageCash.cash[imageUri] = croppedImage
                else
                    nDismissedImages += 1
            }
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
        startActivity(Intent(this, ProcedureActivity::class.java).
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