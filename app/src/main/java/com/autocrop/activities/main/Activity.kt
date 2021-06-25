package com.autocrop.activities.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import com.autocrop.UserPreferences
import com.autocrop.activities.SystemUiHidingFragmentActivity
import com.autocrop.activities.cropping.CroppingActivity
import com.autocrop.activities.examination.N_SAVED_CROPS
import com.autocrop.utils.android.*
import com.autocrop.utils.formattedDateTimeString
import com.autocrop.utils.getByBoolean
import com.autocrop.utils.setSpanHolistically
import com.w2sv.autocrop.R
import kotlinx.android.synthetic.main.activity_main.*
import processing.android.PFragment
import timber.log.Timber
import java.io.File


val SELECTED_IMAGE_URI_STRINGS_IDENTIFIER: String =
    intentExtraIdentifier("selected_image_uri_strings")


class MainActivity : SystemUiHidingFragmentActivity(R.layout.activity_main) {

    private lateinit var userPreferencesOnActivityCreation: List<Boolean>

    override fun onCreate(savedInstanceState: Bundle?) {
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

    private lateinit var flowFieldPApplet: FlowFieldPApplet

    private fun setPixelField() {
        if (!::flowFieldPApplet.isInitialized){
            flowFieldPApplet = FlowFieldPApplet(
                screenResolution(windowManager)
            )
            Timber.i("Initialized flowFieldPApplet")
        }

        PFragment(flowFieldPApplet).setView(
            findViewById<FrameLayout>(R.id.canvas_container), this
        )
    }

    private fun setButtonOnClickListeners() {
        // image selection button
        image_selection_button.setOnClickListener {
            if (!permissionsHandler.allPermissionsGranted)
                permissionsHandler.request()
            else
                selectImages()
        }

        // menu inflation button
        menu_button.setOnClickListener {

            val menuItemToPreferenceKey: Map<Int, String> = mapOf(
                R.id.main_menu_item_delete_input_screenshots to UserPreferences.Keys.deleteInputScreenshots,
                R.id.main_menu_item_save_to_autocropped_dir to UserPreferences.Keys.saveToAutocroppedDir,
                R.id.main_menu_item_conduct_auto_scrolling to UserPreferences.Keys.conductAutoScrolling
            )

            val groupDividerItems: List<Int> = listOf(
                R.id.main_menu_examination_item_group_divider,
                R.id.main_menu_crop_saving_item_group_divider
            )

            // inflate popup menu
            PopupMenu(this, it).run {
                menuInflater.inflate(R.menu.activity_main, menu)

                // set checks from UserPreferences
                menuItemToPreferenceKey.entries.forEach { (key, value) ->
                    menu.findItem(key).isChecked = UserPreferences[value]!!
                }

                // format group divider items
                groupDividerItems.forEach { group_divider_item_id ->
                    with(menu.findItem(group_divider_item_id)){
                        title = SpannableString(title).apply {
                            setSpanHolistically(ForegroundColorSpan(resources.getColor(R.color.saturated_magenta, theme)))
                            setSpanHolistically(StyleSpan(Typeface.ITALIC))
                        }
                    }
                }

                // set item onClickListeners
                setOnMenuItemClickListener { item ->
                    item.run {
                        UserPreferences.toggle(menuItemToPreferenceKey[itemId]!!)
                        isChecked = !isChecked
                        persistMenuAfterItemClick(this)
                    }
                }

                show()
            }
        }

        // flowfield screenshot button
        makeDirIfRequired(flowfieldCapturesDestinationDir.absolutePath)

        flowfield_capture_button.setOnClickListener {
            flowFieldPApplet.canvas.save(
                File(
                    flowfieldCapturesDestinationDir,
                    "flowfield${formattedDateTimeString()}.jpg"
                )
                    .absolutePath
                    .also { Timber.i("Saving flowfield canvas to $it") }
            )

            displayToast(
                "Saved Flowfield Capture to\n${flowfieldCapturesDestinationDir}",
                TextColors.successfullyCarriedOut,
                Toast.LENGTH_SHORT
            )
        }
    }

    private fun displaySavingResultSnackbar(nSavedCrops: Int) {
        val textColor: Int = TextColors.successfullyCarriedOut

        with(UserPreferences.deleteInputScreenshots) {
            when (nSavedCrops) {
                0 -> displaySnackbar("Discarded all crops", textColor)
                1 -> displaySnackbar(
                    listOf(
                        "Saved 1 crop",
                        "Saved 1 crop and deleted\ncorresponding screenshot"
                    ).getByBoolean(this),
                    textColor
                )
                in 2..Int.MAX_VALUE -> displaySnackbar(
                    listOf(
                        "Saved $nSavedCrops crops",
                        "Saved $nSavedCrops crops and deleted\ncorresponding screenshots"
                    ).getByBoolean(this),
                    textColor
                )
                else -> Unit
            }
        }
    }

    val flowfieldCapturesDestinationDir: File = File(
        picturesDir,
        "Flowfield-Captures"
    )

    private val retrieveSnackbarArgument = SnackbarArgumentRetriever()

    // -----------------Permissions---------------------

    private inner class PermissionsHandler{
        private val requiredPermissions: List<String> = listOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        val allPermissionsGranted: Boolean
            get() = permissionToIsGranted.values.all { it }

        private val permissionToIsGranted: MutableMap<String, Boolean> by lazy {
            requiredPermissions
                .associateWith { permissionGranted(it) }
                .toMutableMap()
        }

        fun request() {
            val dummyRequestCode = 420

            requestPermissions(
                requiredPermissions
                    .filter { permissionToIsGranted[it] == false }
                    .toTypedArray(),
                dummyRequestCode
            )
        }

        fun onRequestPermissionsResult(
            permissions: Array<out String>,
            grantResults: IntArray) {

            val permissionToDescription: Map<String, String> = listOf("writing", "reading")
                .mapIndexed { index, s -> requiredPermissions[index] to s }
                .toMap()

            if (grantResults.all { it == PackageManager.PERMISSION_DENIED })
                return displayToast(
                    "You need to permit file reading and\n" +
                            "writing in order for the app to work"
                )

            (permissions zip grantResults.toTypedArray()).forEach {
                if (it.second != PackageManager.PERMISSION_GRANTED)
                    displayToast(
                        "You need to permit file ${permissionToDescription[it.first]}\n" +
                                "in order for the app to work"
                    )
                else
                    permissionToIsGranted[it.first] = true
            }

            if (allPermissionsGranted)
                selectImages()
        }
    }

    private val permissionsHandler = PermissionsHandler()

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        permissionsHandler.onRequestPermissionsResult(permissions, grantResults)
    }

    // -----------------ImageSelection---------------------

    private enum class IntentCode {
        IMAGE_SELECTION
    }

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
                    val cropImagesSelectionMax = 50

                    with(data?.clipData!!) {
                        if (itemCount > cropImagesSelectionMax){
                            displaySnackbar(
                                "Can't crop more than $cropImagesSelectionMax images at a time",
                                TextColors.urgent
                            )
                            return
                        }

                        startCroppingActivity(
                            imageUriStrings = (0 until itemCount)
                                .map { getItemAt(it)?.uri!!.toString() }
                                .toTypedArray()
                        )
                    }
                }
            }
        }
    }

    // -----------------Exiting---------------------

    private fun startCroppingActivity(imageUriStrings: Array<String>) {
        startActivity(
            Intent(this, CroppingActivity::class.java)
                .putExtra(
                    SELECTED_IMAGE_URI_STRINGS_IDENTIFIER,
                    imageUriStrings
                )
        )
        proceedTransitionAnimation()
        onExit()
    }

    /**
     * Triggers app exiting
     */
    override fun onBackPressed() {
        finishAffinity()
    }

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