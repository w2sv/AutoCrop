package com.autocrop.activities.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.FragmentActivity
import com.autocrop.*
import com.autocrop.activities.examination.ExaminationActivity
import com.autocrop.activities.examination.N_SAVED_CROPS
import com.autocrop.activities.hideSystemUI
import com.autocrop.utils.displayToast
import com.autocrop.utils.pathDocument
import com.autocrop.utils.toInt
import com.autocrop.utils.persistMenuAfterItemClick
import com.bunsenbrenner.screenshotboundremoval.BuildConfig
import com.bunsenbrenner.screenshotboundremoval.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import processing.android.PFragment
import java.io.File


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
                    GlobalParameters.deleteInputScreenshots!!
                )
                .apply()
        }
    }

    // -----------------Permissions---------------------

    private enum class Code{
        IMAGE_SELECTION,
        DIRECTORY_SELECTION,

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
                    )[GlobalParameters.deleteInputScreenshots!!.toInt()].toTypedArray()
                )
                in 2..Int.MAX_VALUE -> displayToast(
                    *listOf(
                        listOf("Saved $nSavedCrops crops"),
                        listOf("Saved $nSavedCrops crops and deleted", "corresponding screenshots")
                    )[GlobalParameters.deleteInputScreenshots!!.toInt()].toTypedArray()
                )
            }
        }

        fun setButtonOnClickListeners(){
            image_selection_button.setOnClickListener {
                requestActivityPermissions()

                if (nRequiredPermissions == 0)
                    pickImageFromGallery()
            }

            menu_button.setOnClickListener {
                // inflate popup menu
                PopupMenu(this, it).run {
                    this.menuInflater.inflate(R.menu.main, this.menu)
                    this.menu.findItem(R.id.main_menu_item_delete_input_screenshots).setChecked(
                        GlobalParameters.deleteInputScreenshots!!
                    )

                    this.setOnMenuItemClickListener{ item ->
                        alteredPreferences = true

                        when (item.itemId) {
                            R.id.main_menu_item_delete_input_screenshots -> {
                                // toggle GlobalParameters flag, as well as check mark
                                GlobalParameters.toggleDeleteInputScreenshots()
                                item.setChecked(GlobalParameters.deleteInputScreenshots!!)

                                persistMenuAfterItemClick(item)
                            }

                            R.id.main_menu_item_select_save_directory -> {
                                selectSaveDestinationDirectory()
                            }
                        }
                        true
                    }
                    this.show()
                }
            }
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // set ExaminationActivity.deleteInputScreenshots with corresponding value
        // from shared preferences
        GlobalParameters.deleteInputScreenshots = getSharedPreferences(
            PREFERENCES_INSTANCE_NAME,
            0
        ).getBoolean(PreferencesKey.DELETE_SCREENSHOTS.name, true)

        setPixelField()
        displaySavingResultToast(intent.getIntExtra(N_SAVED_CROPS, -1))
        setButtonOnClickListeners()
    }

    private fun pickImageFromGallery() {
        Intent(Intent.ACTION_PICK).run{
            this.type = "image/*"
            this.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            startActivityForResult(
                this,
                Code.IMAGE_SELECTION.ordinal
            )
        }
    }

    private fun selectSaveDestinationDirectory(){
        with(Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)) {
            this.addCategory(Intent.CATEGORY_DEFAULT)

            this.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            this.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            this.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)

            startActivityForResult(
                Intent.createChooser(
                    this,
                    "Modify directory"),
                Code.DIRECTORY_SELECTION.ordinal
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK){
            when (requestCode) {
                Code.IMAGE_SELECTION.ordinal -> {
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

                Code.DIRECTORY_SELECTION.ordinal -> {
                    fun persistDirectoryPermissions(uri: Uri) {
                        fun loadSavedDirectory() : Uri? = contentResolver.persistedUriPermissions.firstOrNull()?.uri

                        val existing = loadSavedDirectory()
                        if (existing != null) {
                            // Release existing directory when new one is granted
                            contentResolver.releasePersistableUriPermission(existing, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                            contentResolver.releasePersistableUriPermission(existing, Intent.FLAG_GRANT_READ_URI_PERMISSION)

                        }
                        contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                        contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }

                    val documentUri: Uri = data?.data!!.run {
                        // persistDirectoryPermissions(this)
                        this.pathSegments.forEach { println(it) }

                        DocumentsContract.buildDocumentUriUsingTree(
                            this,
                            DocumentsContract.getTreeDocumentId(this)
                        )
                    }.also {
                        println(it)
                        // persistDirectoryPermissions(it)
                    }

                    GlobalParameters.saveDirectoryPath = documentUri.pathDocument(this)
                        .also {
                            println(it)
                            if (BuildConfig.DEBUG && (!(File(it).exists() || !File(it).isDirectory))) {
                                error("Assertion failed")
                            }
                        }
                }
            }
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