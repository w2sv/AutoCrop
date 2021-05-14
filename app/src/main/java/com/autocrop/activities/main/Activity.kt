package com.autocrop.activities.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.widget.PopupMenu
import com.autocrop.UserPreferences
import com.autocrop.activities.SystemUiHidingFragmentActivity
import com.autocrop.activities.cropping.CroppingActivity
import com.autocrop.activities.examination.N_SAVED_CROPS
import com.autocrop.utils.android.*
import com.autocrop.utils.getByBoolean
import com.bunsenbrenner.screenshotboundremoval.R
import kotlinx.android.synthetic.main.activity_main.*
import processing.android.PFragment
import processing.core.PApplet
import timber.log.Timber


val SELECTED_IMAGE_URI_STRINGS_IDENTIFIER: String =
    intentExtraIdentifier("selected_image_uri_strings")


class MainActivity : SystemUiHidingFragmentActivity(R.layout.activity_main) {

    companion object {

        private enum class PermissionCode {
            WRITE,
            READ,
            MULTIPLE;

            companion object {
                operator fun get(index: Int): PermissionCode = values()[index]
            }
        }

        private val REQUIRED_PERMISSIONS: List<String> = listOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        private val PERMISSION_2_DESCRIPTION: Map<String, String> = listOf("writing", "reading")
            .mapIndexed { index, s -> REQUIRED_PERMISSIONS[index] to s }
            .toMap()

        private val PERMISSION_2_CODE: Map<String, PermissionCode> =
            REQUIRED_PERMISSIONS
                .mapIndexed { index, s -> s to PermissionCode[index] }
                .toMap()

        private enum class IntentCode {
            IMAGE_SELECTION
        }

        val MENU_ITEM_ID_2_PREFERENCE_KEY: Map<Int, String> = mapOf(
            R.id.main_menu_item_delete_input_screenshots to UserPreferences.Keys.deleteInputScreenshots,
            R.id.main_menu_item_save_to_autocropped_dir to UserPreferences.Keys.saveToAutocroppedDir,
            R.id.main_menu_conduct_auto_scrolling to UserPreferences.Keys.conductAutoScrolling
        )
    }

    // -----------------Permissions---------------------

    private val permission2IsGranted: MutableMap<String, Boolean> by lazy {
        REQUIRED_PERMISSIONS
            .associateWith { permissionGranted(it) }
            .toMutableMap()
    }

    private val allPermissionsGranted: Boolean
        get() = permission2IsGranted.values.all { it }

    private fun requestActivityPermissions() {
        with(
            REQUIRED_PERMISSIONS
                .filter { permission2IsGranted[it] == false }
                .toTypedArray()
        ) {
            requestPermissions(
                this,
                if (size > 1)
                    PermissionCode.MULTIPLE.ordinal
                else
                    PERMISSION_2_CODE[get(0)]!!.ordinal
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (grantResults.all { it == PackageManager.PERMISSION_DENIED })
            return displayToast(
                "You need to permit file reading and\n" +
                        "writing in order for the app to work"
            )

        (permissions zip grantResults.toTypedArray()).forEach {
            if (it.second != PackageManager.PERMISSION_GRANTED)
                displayToast(
                    "You need to permit file ${PERMISSION_2_DESCRIPTION[it.first]}\n" +
                            "in order for the app to work"
                )
            else
                permission2IsGranted[it.first] = true
        }

        if (allPermissionsGranted)
            selectImages()
    }

    // ------------Lifecycle stages---------------

    private lateinit var flowFieldPApplet: PApplet

    override fun onCreate(savedInstanceState: Bundle?) {
        fun setPixelField() {
            if (!::flowFieldPApplet.isInitialized)
                flowFieldPApplet = FlowFieldPApplet(
                    screenResolution(windowManager)
                ).also {
                    Timber.i("Initialized flowFieldPApplet")
                }

            PFragment(flowFieldPApplet).setView(
                findViewById<FrameLayout>(R.id.canvas_container), this
            )
        }

        fun setButtonOnClickListeners() {
            // image selection button
            image_selection_button.setOnClickListener {
                if (!allPermissionsGranted)
                    requestActivityPermissions()
                else
                    selectImages()
            }

            // menu button
            menu_button.setOnClickListener {
                // inflate popup menu
                PopupMenu(this, it).run {
                    menuInflater.inflate(R.menu.activity_main, menu)

                    // set checks
                    MENU_ITEM_ID_2_PREFERENCE_KEY.entries.forEach { (key, value) ->
                        menu.findItem(key).isChecked = UserPreferences[value]!!
                    }

                    // set item onClickListeners
                    setOnMenuItemClickListener { item ->
                        item.run {
                            UserPreferences.toggle(MENU_ITEM_ID_2_PREFERENCE_KEY[itemId]!!)
                            isChecked = !isChecked
                            persistMenuAfterItemClick(this)
                        }
                    }
                    show()
                }
            }
        }

        fun displaySavingResultSnackbar(nSavedCrops: Int) {
            with(UserPreferences.deleteInputScreenshots) {
                when (nSavedCrops) {
                    0 -> displaySnackbar("Dismissed everything")
                    1 -> displaySnackbar(
                        listOf(
                            "Saved 1 crop",
                            "Saved 1 crop and deleted\ncorresponding screenshot"
                        ).getByBoolean(this)
                    )
                    in 2..Int.MAX_VALUE -> displaySnackbar(
                        listOf(
                            "Saved $nSavedCrops crops",
                            "Saved $nSavedCrops crops and deleted\ncorresponding screenshots"
                        ).getByBoolean(this)
                    )
                }
            }
        }

        super.onCreate(savedInstanceState)
        setPixelField()

        if (!UserPreferences.isInitialized)
            UserPreferences.init(getDefaultSharedPreferences())
        userPreferencesOnActivityCreation = UserPreferences.values.toList()

        setButtonOnClickListeners()
        retrieveSnackbarArgument(intent, N_SAVED_CROPS, -1)?.let {
            displaySavingResultSnackbar(it)
        }
    }

    val retrieveSnackbarArgument = SnackbarArgumentRetriever()

    private fun selectImages() {
        Intent(Intent.ACTION_PICK).run {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
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
                            proceedTransitionAnimation()
                            onExit()
                        }
                    }

                    with(data?.clipData) {
                        startCroppingActivity(
                            imageUriStrings = (0 until this?.itemCount!!).map {
                                getItemAt(it)?.uri!!.toString()
                            }.toTypedArray()
                        )
                    }
                }
            }
        }
    }

    /**
     * Triggers app exiting
     */
    override fun onBackPressed() {
        finishAffinity()
    }

    private lateinit var userPreferencesOnActivityCreation: List<Boolean>

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