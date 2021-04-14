package com.autocrop.activities.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.FragmentActivity
import com.autocrop.*
import com.autocrop.activities.examination.ExaminationActivity
import com.autocrop.activities.examination.N_SAVED_CROPS
import com.autocrop.activities.hideSystemUI
import com.bunsenbrenner.screenshotboundremoval.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import processing.android.PFragment


const val N_DISMISSED_IMAGES: String = "$PACKAGE_NAME.N_DISMISSED_IMAGES"


class MainActivity: FragmentActivity() {
    companion object{
        val imageCash: MutableMap<Uri, Bitmap> = mutableMapOf()

        // ----------Pixel Field---------------

        var pixelField: PixelField? = null
        fun initializePixelField(windowManager: WindowManager){
            Point().run {
                windowManager.defaultDisplay.getRealSize(this)
                pixelField =
                    PixelField(
                        this.x,
                        this.y
                    )
            }
        }
    }

    // ----------------Generic behaviour----------------

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        hideSystemUI(window)
    }

    override fun onBackPressed() {
        finishAffinity()
    }

    var alteredPreferences: Boolean = false

    /**
     * Writes preferences to shared preferences
     */
    override fun onStop() {
        super.onStop()

        if (alteredPreferences){
            getSharedPreferences(PREFERENCES_INSTANCE_NAME, 0)
                .edit().putBoolean(
                    PreferencesKey.DELETE_SCREENSHOTS.name,
                    ExaminationActivity.deleteInputScreenshots!!
                )
                .apply()
        }
    }

    // -----------------Permissions---------------------

    private enum class Code{
        IMAGE_PICK,
        READ_PERMISSION,
        WRITE_PERMISSION
    }

    private var nRequiredPermissions: Int = -1
    private val permission2Code: Map<String, Code> = mapOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE to Code.WRITE_PERMISSION,
        Manifest.permission.READ_EXTERNAL_STORAGE to Code.READ_PERMISSION
    )

    private fun requestActivityPermissions(){
        nRequiredPermissions = 0

        fun checkPermission(permission: String){
            if (checkSelfPermission(permission) == PackageManager.PERMISSION_DENIED){
                nRequiredPermissions ++
                requestPermissions(arrayOf(permission), permission2Code[permission]!!.ordinal)
            }
        }

        checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        fun permissionRequestResultHandling(grantResults: IntArray, requestDescription: String){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_DENIED)
                displayToast(
                    "You need to permit file $requestDescription in order",
                    "for the app to work."
                )
            else
                nRequiredPermissions --
        }

        when (requestCode) {
            Code.READ_PERMISSION.ordinal -> permissionRequestResultHandling(grantResults, "reading")
            Code.WRITE_PERMISSION.ordinal -> permissionRequestResultHandling(
                grantResults,
                "writing"
            )
        }

        if (nRequiredPermissions == 0)
            return pickImageFromGallery()
    }

    // ------------Lifecycle stages---------------

    override fun onStart() {
        super.onStart()
        hideSystemUI(window)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        fun setPixelField(){
            pixelField?.redraw() ?: initializePixelField(windowManager)

            PFragment(pixelField).run {
                this.setView(findViewById<FrameLayout>(R.id.canvas_container), this@MainActivity)
            }
        }

        fun displaySavingResultToast(nSavedCrops: Int){
            when(nSavedCrops){
                0 -> displayToast("Dismissed everything")
                1 -> displayToast(
                    *listOf(
                        listOf("Saved 1 crop"),
                        listOf("Saved 1 crop and deleted", "corresponding screenshot")
                    )[ExaminationActivity.deleteInputScreenshots!!.toInt()].toTypedArray()
                )
                in 2..Int.MAX_VALUE -> displayToast(
                    *listOf(
                        listOf("Saved $nSavedCrops crops"),
                        listOf("Saved $nSavedCrops crops and deleted", "corresponding screenshots")
                    )[ExaminationActivity.deleteInputScreenshots!!.toInt()].toTypedArray()
                )
            }
        }

        fun setButtonOnclickListeners(){
            image_selection_button.setOnClickListener {
                requestActivityPermissions()

                if (nRequiredPermissions == 0)
                    pickImageFromGallery()
            }

            menu_button.setOnClickListener {
                // inflate popup menu
                PopupMenu(this, it).run {
                    this.menuInflater.inflate(R.menu.main, this.menu)
                    this.menu.findItem(R.id.delete_input_screenshots).setChecked(ExaminationActivity.deleteInputScreenshots!!)

                    this.setOnMenuItemClickListener{ item ->
                        alteredPreferences = true

                        when (item.itemId) {
                            R.id.delete_input_screenshots -> {
                                // toggle flag within ExaminationActivity, as well as check mark
                                ExaminationActivity.toggleDeleteInputScreenshots()
                                item.setChecked(ExaminationActivity.deleteInputScreenshots!!)
                            }
                        }
                        persistMenuAfterItemClick(item)
                    }
                    this.show()
                }
            }
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // set ExaminationActivity.deleteInputScreenshots with corresponding value
        // from shared preferences
        ExaminationActivity.deleteInputScreenshots = getSharedPreferences(PREFERENCES_INSTANCE_NAME, 0)
            .getBoolean(PreferencesKey.DELETE_SCREENSHOTS.name, true)

        setPixelField()
        displaySavingResultToast(intent.getIntExtra(N_SAVED_CROPS, -1))
        setButtonOnclickListeners()
    }

        // ------------Options Menu---------------

    private fun pickImageFromGallery() {
        Intent(Intent.ACTION_PICK).run{
            this.type = "image/*"
            this.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            startActivityForResult(
                this,
                Code.IMAGE_PICK.ordinal
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        fun validRequest(): Boolean = resultCode == RESULT_OK && requestCode == Code.IMAGE_PICK.ordinal

        if (validRequest()){
            val nSelectedImages: Int = data?.clipData?.itemCount!!

            for (i in 0 until nSelectedImages) {

                // retrieve uri and resolve into bitmap
                val imageUri: Uri = data.clipData?.getItemAt(i)?.uri!!
                val image: Bitmap? = BitmapFactory.decodeStream(
                    contentResolver.openInputStream(
                        imageUri
                    )
                )

                // attempt to crop image, add uri-crop mapping to image cash if successful
                croppedImage(image!!).run {
                    if (this != null)
                        imageCash[imageUri] = this
                }
            }

            // start ExaminationActivity in case of at least 1 successful crop,
            // otherwise return to image selection screen
            if (imageCash.isNotEmpty())
                startExaminationActivity(nSelectedImages - imageCash.size)
            else
                allImagesDismissedOutput(nSelectedImages > 1)
        }
    }

    // -------------------Follow-up actions-------------------

    private fun startExaminationActivity(dismissedCrops: Int){
        startActivity(
            Intent(this, ExaminationActivity::class.java).putExtra(
                N_DISMISSED_IMAGES,
                dismissedCrops
            )
        )
    }

    private fun allImagesDismissedOutput(attemptedForMultipleImages: Boolean){
        when(attemptedForMultipleImages){
            true -> displayToast("Couldn't find cropping bounds for", "any of the selected images")
            false -> displayToast("Couldn't find cropping bounds for selected image")
        }
    }
}