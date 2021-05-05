package com.autocrop.activities.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.widget.PopupMenu
import com.autocrop.*
import com.autocrop.activities.SystemUiHidingFragmentActivity
import com.autocrop.activities.cropping.CroppingActivity
import com.autocrop.activities.examination.N_SAVED_CROPS
import com.autocrop.utils.*
import com.autocrop.utils.android.*
import com.bunsenbrenner.screenshotboundremoval.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import processing.android.PFragment
import timber.log.Timber
import kotlin.properties.Delegates


val SELECTED_IMAGE_URI_STRINGS_IDENTIFIER: String =
    intentExtraIdentifier("selected_image_uri_strings")


class MainActivity : SystemUiHidingFragmentActivity() {

    companion object {

        // ----------Pixel Field---------------

//        private var pixelField: PixelField? = null
//
//        fun initializePixelField() {
//            pixelField = PixelField()
//        }

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

    private var nRequiredPermissions by Delegates.notNull<Int>()
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
        with(grantResults[0]){
            if (this == PackageManager.PERMISSION_GRANTED){
                nRequiredPermissions--

                selectImagesIfPermissionsGranted()
            }
            else
                displayToast(
                    "You need to permit file ${listOf("reading", "writing")[(this == PermissionCode.WRITE.ordinal).toInt()]}\n" +
                            "in order for the app to work"
                )
        }
    }

    private fun selectImagesIfPermissionsGranted(){
        if (nRequiredPermissions == 0)
            return selectImages()
    }

    // ------------Lifecycle stages---------------

    override fun onCreate(savedInstanceState: Bundle?) {
        fun setPixelField() {
            with(PFragment(
                ParticleFlowField(
                    screenResolution(windowManager)
                )
            )){
                setView(findViewById<FrameLayout>(R.id.canvas_container), this@MainActivity)
                Timber.i("Set PFragment hosting pixel field")
            }
        }

        fun setButtonOnClickListeners() {
            // image selection button
            image_selection_button.setOnClickListener {
                requestActivityPermissions()
                selectImagesIfPermissionsGranted()
            }

            // menu button
            menu_button.setOnClickListener {

                // inflate popup menu
                PopupMenu(this, it).run {
                    menuInflater.inflate(R.menu.activity_main, menu)

                    // set checks
                    menu.findItem(R.id.main_menu_item_delete_input_screenshots).isChecked =
                        UserPreferences.deleteInputScreenshots
                    menu.findItem(R.id.main_menu_item_save_to_autocrop_folder).isChecked =
                        UserPreferences.saveToAutocropDir

                    // set item onClickListeners
                    this.setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            // input screenshot deleting
                            R.id.main_menu_item_delete_input_screenshots -> {
                                UserPreferences.toggle(PreferenceParameter.DeleteInputScreenshots)
                                item.isChecked = UserPreferences.deleteInputScreenshots

                                persistMenuAfterItemClick(item)
                            }

                            // saving to dedicated directory
                            R.id.main_menu_item_save_to_autocrop_folder -> {
                                UserPreferences.toggle(PreferenceParameter.SaveToAutocropDir)

                                item.isChecked = UserPreferences.saveToAutocropDir

                                persistMenuAfterItemClick(item)
                            }
                        }
                        false
                    }
                    show()
                }
            }
        }

        fun displayToasts(){
            fun displaySavingResultToast(nSavedCrops: Int) {
                when (nSavedCrops) {
                    0 -> displayToast("Dismissed everything")
                    1 -> displayToast(
                        listOf(
                            "Saved 1 crop",
                            "Saved 1 crop and deleted\ncorresponding screenshot"
                        )[UserPreferences.deleteInputScreenshots.toInt()]
                    )
                    in 2..Int.MAX_VALUE -> displayToast(
                        listOf(
                            "Saved $nSavedCrops crops",
                            "Saved $nSavedCrops crops and deleted\ncorresponding screenshots"
                        )[UserPreferences.deleteInputScreenshots.toInt()]
                    )
                }
            }

            // display either saving result toast if returning from examination activity or all images dismissed
            // toast if returning from cropping activity or nothing if none of the former applying
            with(intent.getIntExtra(N_SAVED_CROPS, -1)) {
                if (this != -1)
                    displaySavingResultToast(this)
            }
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setPixelField()

        if (!UserPreferences.isInitialized)
            UserPreferences.initialize(getDefaultSharedPreferences())
        userPreferencesOnActivityCreation = UserPreferences.clone()

        setButtonOnClickListeners()
        displayToasts()
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
                        ).also {
                            onExit()
                        }
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

    private lateinit var userPreferencesOnActivityCreation: Array<Boolean>

    /**
     * Writes set preferences to shared preferences
     * in case of them having been altered
     */
    private fun onExit() {
        UserPreferences.writeToSharedPreferences(
            userPreferencesOnActivityCreation,
            getDefaultSharedPreferences()
        )
    }
}