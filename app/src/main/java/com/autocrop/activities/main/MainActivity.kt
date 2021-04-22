package com.autocrop.activities.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Point
import android.os.Bundle
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.appcompat.widget.PopupMenu
import com.autocrop.*
import com.autocrop.activities.SystemUiHidingFragmentActivity
import com.autocrop.activities.cropping.CroppingActivity
import com.autocrop.activities.cropping.DismissedImagesQuantity
import com.autocrop.activities.examination.N_SAVED_CROPS
import com.autocrop.activities.hideSystemUI
import com.autocrop.utils.*
import com.autocrop.utils.android.*
import com.bunsenbrenner.screenshotboundremoval.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import processing.android.PFragment
import timber.log.Timber
import timber.log.Timber.DebugTree


val SELECTED_IMAGE_URI_STRINGS_IDENTIFIER: String =
    intentExtraIdentifier("selected_image_uri_strings")


class MainActivity : SystemUiHidingFragmentActivity() {

    companion object {

        // ----------Pixel Field---------------

        var pixelField: PixelField? = null
        fun initializePixelField(windowManager: WindowManager) {
            with(Point()) {
                windowManager.defaultDisplay.getRealSize(this).also {
                    Timber.i("Screen resolution: $this")
                }
                pixelField = PixelField(
                        x,
                        y
                    )
            }
        }

        // -------------Codes---------------

        private enum class PermissionCode {
            READ,
            WRITE
        }

        private enum class IntentCode {
            IMAGE_SELECTION
        }
    }

    // ----------------Generic behaviour----------------

    /**
     * Triggers app exiting
     */
    override fun onBackPressed() {
        finishAffinity()
    }

    // -----------------Permissions---------------------

    private var nRequiredPermissions: Int = -1
    private val permission2Code: Map<String, PermissionCode> = mapOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE to PermissionCode.WRITE,
        Manifest.permission.READ_EXTERNAL_STORAGE to PermissionCode.READ
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
            PermissionCode.READ.ordinal -> permissionRequestResultHandling(
                grantResults,
                "reading"
            )
            PermissionCode.WRITE.ordinal -> permissionRequestResultHandling(
                grantResults,
                "writing"
            )
        }

        // directly go into image selection after permission granting
        if (nRequiredPermissions == 0)
            return selectImages()
    }

    // ------------Lifecycle stages---------------

    override fun onCreate(savedInstanceState: Bundle?) {

        fun setPixelField() {
            pixelField?.redraw()
                .also { Timber.i("Redrew pixel field") }
            ?: initializePixelField(windowManager)
                .also { Timber.i("Initialized pixel field") }

            PFragment(pixelField).run {
                setView(findViewById<FrameLayout>(R.id.canvas_container), this@MainActivity)
            }
        }

        fun displayPreviousActivityResultToast() {
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
                            listOf(
                                "Saved $nSavedCrops crops and deleted",
                                "corresponding screenshots"
                            )
                        )[GlobalParameters.deleteInputScreenshots.toInt()].toTypedArray()
                    )
                }
            }

            fun displayAllImagesDismissedToast(dismissedImagesQuantity: DismissedImagesQuantity) {
                when (dismissedImagesQuantity) {
                    DismissedImagesQuantity.Multiple -> displayToast(
                        "Couldn't find cropping bounds for",
                        "any of the selected images"
                    )
                    DismissedImagesQuantity.One -> displayToast("Couldn't find cropping bounds for selected image")
                }
            }

            // display either saving result toast if returning from examination activity or all images dismissed
            // toast if returning from cropping activity or nothing if none of the former applying
            with(intent.getIntExtra(N_SAVED_CROPS, -1)) {
                if (this != -1)
                    displaySavingResultToast(this)
                else {
                    with(intent.getEnumExtra<DismissedImagesQuantity>()) {
                        if (this != null)
                            displayAllImagesDismissedToast(this)
                    }
                }
            }
        }

        fun besetGlobalParametersFromSharedPreferences() {
            GlobalParameters.deleteInputScreenshots = getSharedPreferencesBool(
                SharedPreferencesKey.DELETE_SCREENSHOTS,
                false
            )
            Timber.i("Set GlobalParameters.deleteInputScreenshots to ${GlobalParameters.deleteInputScreenshots}")

            GlobalParameters.saveToAutocropDir = getSharedPreferencesBool(
                SharedPreferencesKey.SAVE_TO_AUTOCROP_DIR,
                false
            )
            Timber.i("Set GlobalParameters.saveToAutocropDir to ${GlobalParameters.saveToAutocropDir}")
        }

        fun setButtonOnClickListeners() {
            // image selection button
            image_selection_button.setOnClickListener {
                requestActivityPermissions()

                if (nRequiredPermissions == 0)
                    selectImages()
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
                                Timber.i("Toggled GlobalParameters.deleteInputScreenshots to ${GlobalParameters.deleteInputScreenshots}")

                                item.isChecked = GlobalParameters.deleteInputScreenshots

                                persistMenuAfterItemClick(item)
                            }

                            // saving to dedicated directory
                            R.id.main_menu_item_save_to_autocrop_folder -> {
                                GlobalParameters.toggleSaveToAutocropDir()
                                Timber.i("Toggled GlobalParameters.saveToAutoCropDir to ${GlobalParameters.saveToAutocropDir}")

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

        if (debuggingMode())
            Timber.plant(DebugTree())
        setPixelField()
        besetGlobalParametersFromSharedPreferences()
        setButtonOnClickListeners()
        displayPreviousActivityResultToast()
    }

    private fun selectImages() {
        Intent(Intent.ACTION_PICK).run {
            this.type = "image/*"
            this.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            startActivityForResult(
                this,
                IntentCode.IMAGE_SELECTION.ordinal
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                IntentCode.IMAGE_SELECTION.ordinal -> {

                    fun startCroppingActivity(imageUriStrings: Array<String>) {
                        startActivity(
                            Intent(this, CroppingActivity::class.java)
                                .putExtra(
                                    SELECTED_IMAGE_URI_STRINGS_IDENTIFIER,
                                    imageUriStrings
                                )
                        )
                    }

                    startCroppingActivity(
                        imageUriStrings = (0 until data?.clipData?.itemCount!!).map {
                            data.clipData?.getItemAt(it)?.uri!!.toString()
                        }.toTypedArray()
                    )
                }
            }
        }
    }

    private var alteredPreferences: Boolean = false

    /**
     * Writes set preferences to shared preferences
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
                SharedPreferencesKey.SAVE_TO_AUTOCROP_DIR,
                GlobalParameters.saveToAutocropDir
            )
        }
    }
}