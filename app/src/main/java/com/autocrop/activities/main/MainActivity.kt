package com.autocrop.activities.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Bundle
import android.os.Environment
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import com.autocrop.UserPreferences
import com.autocrop.activities.IntentIdentifiers
import com.autocrop.activities.SystemUiHidingFragmentActivity
import com.autocrop.activities.cropping.CroppingActivity
import com.autocrop.utils.android.*
import com.autocrop.utils.formattedDateTimeString
import com.autocrop.utils.get
import com.autocrop.utils.setSpanHolistically
import com.w2sv.autocrop.R
import com.w2sv.autocrop.databinding.ActivityMainBinding
import processing.android.PFragment
import timber.log.Timber
import java.io.File

class MainActivity : SystemUiHidingFragmentActivity() {

    companion object{
        const val CROP_IMAGES_SELECTION_MAX: Int = 100
    }

    enum class IntentCodes{
        IMAGE_SELECTION
    }

    lateinit var flowField: FlowField

    private val nSavedCropsRetriever = IntentExtraRetriever()

    private lateinit var binding: ActivityMainBinding

    /**
     * - Sets flowfield
     * - Initializes UserPreferences from shared preferences
     * - Sets button onClickListeners
     * - Displays crop saving results snackbar if applicable
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!::flowField.isInitialized)
            flowField = FlowField()
        flowField.setPFragment()

        // initialize UserPreferences if necessary
        if (!UserPreferences.isInitialized)
            UserPreferences.init(getSharedPreferences(UserPreferences.sharedPreferencesFileName))

        // set button onClickListeners
        setImageSelectionButtonOnClickListener()
        setMenuInflationButtonOnClickListener()
        flowField.setCaptureButtonOnClickListener()

        // display cropping saving results if applicable
        nSavedCropsRetriever(intent, IntentIdentifiers.N_SAVED_CROPS, -1)?.let { nSavedCrops ->
            displaySnackbar(
                when (nSavedCrops) {
                    0 -> "Discarded all crops"
                    else -> listOf("", "s")[nSavedCrops >= 2].let { numerusInflection ->
                        "Saved $nSavedCrops crop$numerusInflection" + listOf("", " and deleted\n$nSavedCrops corresponding screenshot$numerusInflection")[UserPreferences.deleteScreenshotsOnSaveAll]
                    }
                },
                textColor = TextColors.successfullyCarriedOut
            )
        }
    }

    inner class FlowField{
        private val pApplet: FlowFieldPApplet = FlowFieldPApplet(
            screenResolution(windowManager)
        ).also { Timber.i("Initialized flowFieldPApplet") }

        fun setPFragment() {
            PFragment(pApplet).setView(
                binding.canvasContainer, this@MainActivity
            )
        }

        /**
         * Not working for api > Q, such that image simply won't be saved,
         * however without leading to a crash
         *
         * https://stackoverflow.com/questions/36088699/error-open-failed-enoent-no-such-file-or-directory
         */
        val flowfieldDestinationDir: File = if (apiNotNewerThanQ)
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        else
            applicationContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!

        fun setCaptureButtonOnClickListener(){
            binding.flowfieldCaptureButton.setOnClickListener {
                File(flowfieldDestinationDir, "FlowField_${formattedDateTimeString()}")
                    .absolutePath
                    .let { destinationFilePath ->
                        pApplet.canvas.save(destinationFilePath)
                        Timber.i("Saving flowfield canvas to $destinationFilePath")
                    }
                displaySnackbar(
                    "Saved Flowfield Capture to\n${flowfieldDestinationDir.absolutePath}",
                    TextColors.successfullyCarriedOut,
                    Toast.LENGTH_SHORT
                )
            }
        }
    }

    fun setMenuInflationButtonOnClickListener(){
        binding.menuButton.setOnClickListener { view: View ->

            val menuItemToPreferenceKey: Map<Int, String> = mapOf(
                R.id.main_menu_item_conduct_auto_scrolling to UserPreferences.Keys.conductAutoScrolling
            )

            val groupDividerItems: List<Int> = listOf(
                R.id.main_menu_examination_item_group_divider
            )

            // inflate popup menu
            PopupMenu(this@MainActivity, view).run {
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
    }

    //$$$$$$$$$$$$$$
    // Permissions $
    //$$$$$$$$$$$$$$
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
    ) = permissionsHandler.onRequestPermissionsResult(permissions, grantResults)

    //$$$$$$$$$$$$$$$$$$
    // Image Selection $
    //$$$$$$$$$$$$$$$$$$
    private fun setImageSelectionButtonOnClickListener(){
        binding.imageSelectionButton.setOnClickListener {
            if (!permissionsHandler.allPermissionsGranted)
                permissionsHandler.request()
            else
                selectImages()
        }
    }

    private fun selectImages() {
        Intent(Intent.ACTION_PICK).run {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            startActivityForResult(
                this,
                IntentCodes.IMAGE_SELECTION.ordinal
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                IntentCodes.IMAGE_SELECTION.ordinal -> {
                    with(data?.clipData!!) {
                        if (itemCount > CROP_IMAGES_SELECTION_MAX){
                            displaySnackbar(
                                "Can't crop more than $CROP_IMAGES_SELECTION_MAX images at a time",
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

    //$$$$$$$$$$
    // Exiting $
    //$$$$$$$$$$
    private fun startCroppingActivity(imageUriStrings: Array<String>) {
        startActivity(
            Intent(this, CroppingActivity::class.java)
                .putExtra(
                    IntentIdentifiers.SELECTED_IMAGE_URI_STRINGS,
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
     */
    private fun onExit() = UserPreferences.writeToSharedPreferences(getSharedPreferences(UserPreferences.sharedPreferencesFileName))
}