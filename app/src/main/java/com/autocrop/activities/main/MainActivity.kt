package com.autocrop.activities.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Bundle
import android.os.Environment
import android.os.Parcelable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import com.autocrop.UserPreferences
import com.autocrop.activities.IntentIdentifiers
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

class MainActivity : AppCompatActivity() {

    companion object{
        const val CROP_IMAGES_SELECTION_MAX: Int = 100

        enum class IntentCodes{
            IMAGE_SELECTION
        }
    }

    lateinit var flowFieldHandler: FlowFieldHandler

    private val nSavedCropsRetriever = IntentExtraRetriever<IntArray>()

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

        if (!::flowFieldHandler.isInitialized)
            flowFieldHandler = FlowFieldHandler()
        flowFieldHandler.setPFragment()

        // initialize UserPreferences if necessary
        if (!UserPreferences.isInitialized)
            UserPreferences.initializeFromSharedPreferences(getSharedPreferences())

        // set button onClickListeners
        setImageSelectionButtonOnClickListener()
        setMenuInflationButtonOnClickListener()
        flowFieldHandler.setCaptureButtonOnClickListener()

        // display cropping saving results if applicable
        nSavedCropsRetriever(intent, IntentIdentifiers.N_SAVED_CROPS_WITH_N_DELETED_SCREENSHOTS)?.let {
            val (nSavedCrops, nDeletedScreenshots) = it[0] to it[1]

            displaySnackbar(
                when (nSavedCrops) {
                    0 -> "Discarded all crops"
                    else -> "Saved $nSavedCrops crop${numberInflection(nSavedCrops)}".run {
                        if (nDeletedScreenshots != 0)
                            plus(" and deleted\n${listOf(nDeletedScreenshots, "corresponding")[nDeletedScreenshots == nSavedCrops]} screenshot${numberInflection(nDeletedScreenshots)}")
                        else
                            this
                    }
                },
                TextColors.SUCCESS
            )
        }
    }

    inner class FlowFieldHandler{
        private val pApplet: FlowFieldPApplet = FlowFieldPApplet(
            screenResolution(windowManager)
        )

        fun setPFragment() {
            PFragment(pApplet).setView(
                binding.canvasContainer, this@MainActivity
            )
        }

        private val flowfieldDestinationDir: File = if (apiNotNewerThanQ)
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        else
            /**
             * Not working for api > Q, such that image simply won't be saved,
             * however without leading to a crash
             *
             * https://stackoverflow.com/questions/36088699/error-open-failed-enoent-no-such-file-or-directory
             */
            applicationContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!

        /**
         * Request permissions if necessary and run [captureFlowField] if granted or
         * directly run [captureFlowField] respectively
         */
        fun setCaptureButtonOnClickListener() = binding.flowfieldCaptureButton.setOnClickListener {
                permissionsHandler.requestPermissionsIfNecessaryAndRunFunIfAllGrantedOrRunDirectly {
                    captureFlowField()
                }
            }

        /**
         * Save current FlowField canvas to "{ExternalImageDirectory}.{FlowField_{formattedDateTimeString()}}.jpg",
         * display Snackbar comprising directory path file has been saved to
         */
        private fun captureFlowField(){
            File(flowfieldDestinationDir, "FlowField_${formattedDateTimeString()}.jpg")
                .canonicalPath
                .let { destinationFilePath ->
                    pApplet.canvas.save(destinationFilePath)
                    Timber.i("Saving flowfield canvas to $destinationFilePath")
                }
            displaySnackbar(
                "Saved Flowfield Capture to\n${flowfieldDestinationDir.absolutePath}",
                TextColors.SUCCESS,
                Toast.LENGTH_SHORT
            )
        }
    }

    private fun setMenuInflationButtonOnClickListener(){
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
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        private val missingPermissions: List<String>
            get() = requiredPermissions.filter { !permissionGranted(it) }

        /**
         * Decorator either running passed function if all permissions granted, otherwise sets
         * [onAllPermissionsGranted] to passed function and calls [requestPermissions]
         */
        fun requestPermissionsIfNecessaryAndRunFunIfAllGrantedOrRunDirectly(onAllPermissionsGranted: () -> Unit): Unit =
            missingPermissions.let {
                if (it.isNotEmpty()){
                    this.onAllPermissionsGranted = onAllPermissionsGranted
                    return requestPermissions(it.toTypedArray(), 420)
                }
                return onAllPermissionsGranted()
            }

        private var onAllPermissionsGranted: (() -> Unit)?  = null

        /**
         * Display snackbar if any permission hasn't been granted,
         * otherwise run [onAllPermissionsGranted], which needs to have been set previously
         *
         * Clears [onAllPermissionsGranted] afterwards in any case
         */
        fun onRequestPermissionsResult(permissions: Array<out String>, grantResults: IntArray) {
            if (grantResults.any { it == PackageManager.PERMISSION_DENIED }) {
                displaySnackbar(
                    "You need to permit file reading and\n" +
                            "writing in order for the app to work",
                    TextColors.NEUTRAL
                )
                Timber.i("Not all required permissions were granted; permissions: ${permissions.toList()} | grantResults: ${grantResults.toList()}")
            }
            else
                onAllPermissionsGranted!!()
            onAllPermissionsGranted = null
        }
    }

    private val permissionsHandler = PermissionsHandler()

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        permissionsHandler.onRequestPermissionsResult(permissions, grantResults)
    }

    //$$$$$$$$$$$$$$$$$$
    // Image Selection $
    //$$$$$$$$$$$$$$$$$$

    /**
     * Run [selectImages] if all permissions granted, otherwise request required permissions and
     * then run [selectImages] if all granted
     */
    private fun setImageSelectionButtonOnClickListener() = binding.imageSelectionButton.setOnClickListener {
            permissionsHandler.requestPermissionsIfNecessaryAndRunFunIfAllGrantedOrRunDirectly {
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
                        if (itemCount > CROP_IMAGES_SELECTION_MAX)
                            displaySnackbar(
                                "Can't crop more than $CROP_IMAGES_SELECTION_MAX images at a time",
                                TextColors.URGENT
                            )
                        else
                            startCroppingActivity(imageUris = ArrayList((0 until itemCount).map { getItemAt(it)?.uri!! }))
                    }
                }
            }
        }
    }

    //$$$$$$$$$$
    // Exiting $
    //$$$$$$$$$$
    private fun startCroppingActivity(imageUris: ArrayList<Parcelable>) {
        startActivity(
            Intent(this, CroppingActivity::class.java)
                .putParcelableArrayListExtra(IntentIdentifiers.SELECTED_IMAGE_URI_STRINGS, imageUris)
        )
        proceedTransitionAnimation()
    }

    /**
     * Triggers app exiting
     */
    override fun onBackPressed() {
        finishAffinity()
    }

    /**
     * Write set preferences to shared preferences
     */
    override fun onPause() {
        super.onPause()

        UserPreferences.writeChangedValuesToSharedPreferences(lazy { getSharedPreferences() })
    }
}