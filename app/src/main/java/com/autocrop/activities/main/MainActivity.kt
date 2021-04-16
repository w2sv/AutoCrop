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
import com.autocrop.utils.*
import com.autocrop.utils.android.*
import com.bunsenbrenner.screenshotboundremoval.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import processing.android.PFragment


const val N_DISMISSED_IMAGES: String = "$PACKAGE_NAME.N_DISMISSED_IMAGES"


class MainActivity : FragmentActivity() {

    companion object {

        // ----------Pixel Field---------------

        var pixelField: PixelField? = null
        fun initializePixelField(windowManager: WindowManager) {
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

    private enum class Code {
        IMAGE_SELECTION_INTENT,

        READ_PERMISSION,
        WRITE_PERMISSION
    }

    // ----------------Generic behaviour----------------

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        hideSystemUI(window)
    }

    /**
     * Exits app
     */
    override fun onBackPressed() {
        finishAffinity()
    }

    // -----------------Permissions---------------------

    private var nRequiredPermissions: Int = -1
    private val permission2Code: Map<String, Code> = mapOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE to Code.WRITE_PERMISSION,
        Manifest.permission.READ_EXTERNAL_STORAGE to Code.READ_PERMISSION
    )

    private fun requestActivityPermissions() {
        nRequiredPermissions = 0

        fun checkPermission(permission: String) {
            if (checkSelfPermission(permission) == PackageManager.PERMISSION_DENIED) {
                nRequiredPermissions++
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
        fun permissionRequestResultHandling(grantResults: IntArray, requestDescription: String) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_DENIED)
                displayToast(
                    "You need to permit file $requestDescription in order",
                    "for the app to work."
                )
            else
                nRequiredPermissions--
        }

        when (requestCode) {
            Code.READ_PERMISSION.ordinal -> permissionRequestResultHandling(
                grantResults,
                "reading"
            )
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
        fun setPixelField() {
            pixelField?.redraw() ?: initializePixelField(windowManager)

            PFragment(pixelField).run {
                this.setView(findViewById<FrameLayout>(R.id.canvas_container), this@MainActivity)
            }
        }

        fun displaySavingResultToast(nSavedCrops: Int) {
            when (nSavedCrops) {
                0 -> displayToast("Dismissed everything")
                1 -> displayToast(
                    *listOf(
                        listOf("Saved 1 crop"),
                        listOf("Saved 1 crop and deleted", "corresponding screenshot")
                    )[GlobalParameters.deleteInputScreenshots.toInt()].toTypedArray()
                )
                in 2..Int.MAX_VALUE -> displayToast(
                    *listOf(
                        listOf("Saved $nSavedCrops crops"),
                        listOf("Saved $nSavedCrops crops and deleted", "corresponding screenshots")
                    )[GlobalParameters.deleteInputScreenshots.toInt()].toTypedArray()
                )
            }
        }

        fun besetGlobalParameters() {
            GlobalParameters.deleteInputScreenshots = getSharedPreferencesBool(
                SharedPreferencesKey.DELETE_SCREENSHOTS,
                false
            )
            GlobalParameters.saveToAutocropDir = getSharedPreferencesBool(
                SharedPreferencesKey.SAVE_TO_AUTOCROP_FOLDER,
                false
            )
        }

        fun setButtonOnClickListeners() {
            // image selection button
            image_selection_button.setOnClickListener {
                requestActivityPermissions()

                if (nRequiredPermissions == 0)
                    pickImageFromGallery()
            }

            // menu button
            menu_button.setOnClickListener {

                // inflate popup menu
                PopupMenu(this, it).run {
                    this.menuInflater.inflate(R.menu.activity_main, this.menu)

                    // set checks
                    this.menu.findItem(R.id.main_menu_item_delete_input_screenshots).isChecked =
                        GlobalParameters.deleteInputScreenshots
                    this.menu.findItem(R.id.main_menu_item_save_to_autocrop_folder).isChecked =
                        GlobalParameters.saveToAutocropDir

                    // set item onClickListeners
                    this.setOnMenuItemClickListener { item ->
                        alteredPreferences = true

                        when (item.itemId) {
                            // input screenshot deleting
                            R.id.main_menu_item_delete_input_screenshots -> {
                                GlobalParameters.deleteInputScreenshots =
                                    !GlobalParameters.deleteInputScreenshots
                                item.isChecked = GlobalParameters.deleteInputScreenshots

                                persistMenuAfterItemClick(item)
                            }

                            // saving to dedicated directory
                            R.id.main_menu_item_save_to_autocrop_folder -> {
                                GlobalParameters.toggleSaveToDedicatedDir()
                                item.isChecked = GlobalParameters.saveToAutocropDir

                                persistMenuAfterItemClick(item)
                            }
                        }
                        false
                    }
                    this.show()
                }
            }
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setPixelField()
        displaySavingResultToast(intent.getIntExtra(N_SAVED_CROPS, -1))
        besetGlobalParameters()
        setButtonOnClickListeners()
    }

    private fun pickImageFromGallery() {
        Intent(Intent.ACTION_PICK).run {
            this.type = "image/*"
            this.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            startActivityForResult(
                this,
                Code.IMAGE_SELECTION_INTENT.ordinal
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                Code.IMAGE_SELECTION_INTENT.ordinal -> {
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
                        with(croppedImage(image!!)) {
                            if (this != null)
                                GlobalParameters.imageCash[imageUri] = this
                        }
                    }

                    // start ExaminationActivity in case of at least 1 successful crop,
                    // otherwise return to image selection screen
                    if (GlobalParameters.imageCash.isNotEmpty())
                        startExaminationActivity(nSelectedImages - GlobalParameters.imageCash.size)
                    else
                        allImagesDismissedOutput(nSelectedImages > 1)
                }
            }
        }
    }

    // -------------------Follow-up actions-------------------

    private fun startExaminationActivity(dismissedCrops: Int) {
        startActivity(
            Intent(this, ExaminationActivity::class.java).putExtra(
                N_DISMISSED_IMAGES,
                dismissedCrops
            )
        )
    }

    private fun allImagesDismissedOutput(attemptedForMultipleImages: Boolean) {
        when (attemptedForMultipleImages) {
            true -> displayToast("Couldn't find cropping bounds for", "any of the selected images")
            false -> displayToast("Couldn't find cropping bounds for selected image")
        }
    }

    var alteredPreferences: Boolean = false

    /**
     * Writes preferences to shared preferences
     * in case of them having been altered
     */
    override fun onStop() {
        super.onStop()

        if (alteredPreferences) {
            writeSharedPreferencesBool(
                SharedPreferencesKey.DELETE_SCREENSHOTS,
                GlobalParameters.deleteInputScreenshots
            )
            writeSharedPreferencesBool(
                SharedPreferencesKey.SAVE_TO_AUTOCROP_FOLDER,
                GlobalParameters.saveToAutocropDir
            )
        }
    }
}