package com.autocrop.activities.main.fragments.flowfield

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.PopupMenu
import com.autocrop.activities.ActivityTransitions
import com.autocrop.activities.IntentIdentifiers
import com.autocrop.activities.cropping.CroppingActivity
import com.autocrop.activities.main.fragments.MainActivityFragment
import com.autocrop.global.BooleanUserPreferences
import com.autocrop.global.SaveDestinationPreferences
import com.autocrop.utils.android.*
import com.autocrop.utils.formattedDateTimeString
import com.autocrop.utils.setSpanHolistically
import com.w2sv.autocrop.R
import com.w2sv.autocrop.databinding.ActivityMainFragmentFlowfieldBinding
import processing.android.PFragment
import timber.log.Timber

class FlowFieldFragment: MainActivityFragment<ActivityMainFragmentFlowfieldBinding>() {

    companion object{
        private val REQUIRED_PERMISSIONS: Array<String> = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    private lateinit var flowFieldHandler: FlowFieldHandler

    private val permissionsHandler = PermissionsHandler()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // initialize FlowField
        flowFieldHandler = FlowFieldHandler()

        // set button onClickListeners
        setImageSelectionButtonOnClickListener()
        setMenuInflationButtonOnClickListener()
        flowFieldHandler.setCaptureButtonOnClickListener()
    }

    private inner class FlowFieldHandler{
        val pApplet: FlowFieldPApplet =
            FlowFieldPApplet(
                screenResolution(requireActivity().windowManager)
            )

        init {
            PFragment(pApplet).setView(binding.canvasContainer, requireActivity())
        }

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
         * Save current FlowField canvas to "[externalPicturesDir].{FlowField_[formattedDateTimeString]}.jpg",
         * display Snackbar with saving destination
         */
        private fun captureFlowField(){
            pApplet.bitmap().save(requireContext().contentResolver,"FlowField_${formattedDateTimeString()}.jpg")

            requireActivity().displaySnackbar(
                "Saved FlowField to \n$externalPicturesDir",
                TextColors.SUCCESS,
                Toast.LENGTH_SHORT
            )
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)

        if (hidden)
            flowFieldHandler.pApplet.pause()
        else
            flowFieldHandler.pApplet.resume()
    }

    private fun setMenuInflationButtonOnClickListener() =
        binding.menuButton.setOnClickListener { view: View ->

            // inflate popup menu
            PopupMenu(requireContext(), view).run {
                menuInflater.inflate(R.menu.activity_main, menu)

                // set checks from UserPreferences
                mapOf(
                    R.id.main_menu_item_conduct_auto_scrolling to BooleanUserPreferences.Keys.conductAutoScrolling
                ).forEach { (id, userPreferencesKey) ->
                    with(menu.findItem(id)){
                        isChecked = BooleanUserPreferences.getValue(userPreferencesKey)

                        setOnMenuItemClickListener { item ->
                            BooleanUserPreferences.toggle(userPreferencesKey)
                            isChecked = !isChecked
                            item.persistMenuAfterClick(requireContext())
                        }
                    }
                }

                mapOf(
                    R.id.main_menu_item_rate_the_app to ::goToPlayStoreListing,
                    R.id.main_menu_item_about_the_app to { with(typedActivity) { hideAndShowFragments(rootFragment, aboutFragment) } },
                    R.id.main_menu_item_change_save_destination_dir to { pickSaveDestinationDir.launch(SaveDestinationPreferences.treeUri) }
                ).forEach { (id, onClickListener) ->
                    with(menu.findItem(id)){
                        setOnMenuItemClickListener {
                            onClickListener()
                            true
                        }
                    }
                }

                // format group divider items
                listOf(
                    R.id.main_menu_group_divider_examination,
                    R.id.main_menu_group_divider_crop_saving,
                    R.id.main_menu_group_divider_other
                ).forEach { id ->
                    with(menu.findItem(id)){
                        title = SpannableString(title).apply {
                            setSpanHolistically(ForegroundColorSpan(resources.getColor(R.color.saturated_magenta, requireContext().theme)))
                            setSpanHolistically(StyleSpan(Typeface.ITALIC))
                        }
                    }
                }
                menu.makeIconsVisible()
                show()
            }
        }

    private fun goToPlayStoreListing() =
        startActivity(
            Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://play.google.com/store/apps/details?id=${requireContext().packageName}")
                setPackage("com.android.vending")
            }
        )

    private val pickSaveDestinationDir = registerForActivityResult(
        object: ActivityResultContracts.OpenDocumentTree(){
            override fun createIntent(context: Context, input: Uri?): Intent {
                return super.createIntent(context, input)
                    .apply {
                        flags = Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
                    }
            }
        }
    ) {
        it?.let { treeUri ->
            requireContext().contentResolver.takePersistableUriPermission(treeUri,Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            SaveDestinationPreferences.treeUri = treeUri
        }
    }

    //$$$$$$$$$$$$$$
    // Permissions $
    //$$$$$$$$$$$$$$
    private inner class PermissionsHandler{
        private val missingPermissions: List<String>
            get() = REQUIRED_PERMISSIONS.filter { !requireActivity().permissionGranted(it) }

        private val permissionRequestContract = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){
            onRequestPermissionsResult(it)
        }

        /**
         * Decorator either running passed function if all permissions granted, otherwise sets
         * [onAllPermissionsGranted] to passed function and calls [requestPermissions]
         */
        fun requestPermissionsIfNecessaryAndRunFunIfAllGrantedOrRunDirectly(onAllPermissionsGranted: () -> Unit): Unit =
            missingPermissions.let {
                if (it.isNotEmpty()){
                    this.onAllPermissionsGranted = onAllPermissionsGranted
                    permissionRequestContract.launch(it.toTypedArray())
                }
                else
                    onAllPermissionsGranted()
            }

        private var onAllPermissionsGranted: (() -> Unit)?  = null

        /**
         * Display snackbar if any permission hasn't been granted,
         * otherwise run [onAllPermissionsGranted], which needs to have been set previously
         *
         * Clears [onAllPermissionsGranted] afterwards in any case
         */
        fun onRequestPermissionsResult(permissionToGranted: Map<String, Boolean>) {
            if (permissionToGranted.values.any { !it }) {
                requireActivity().displaySnackbar(
                    "You need to permit file reading and\nwriting in order for the app to work",
                    TextColors.NEUTRAL
                )
                Timber.i("Not all required permissions were granted: $permissionToGranted")
            }
            else
                onAllPermissionsGranted!!()
            onAllPermissionsGranted = null
        }
    }

    //$$$$$$$$$$$$$$$$$$
    // Image Selection $
    //$$$$$$$$$$$$$$$$$$

    /**
     * Launch [selectImagesContract] if all permissions granted, otherwise request required permissions and
     * then launch [selectImagesContract] if all granted
     */
    private fun setImageSelectionButtonOnClickListener() = binding.imageSelectionButton.setOnClickListener {
        permissionsHandler.requestPermissionsIfNecessaryAndRunFunIfAllGrantedOrRunDirectly {
            selectImagesContract.launch(IMAGE_MIME_TYPE)
        }
    }

    private val selectImagesContract = registerForActivityResult(ActivityResultContracts.GetMultipleContents()){ selectedUris ->
        if (selectedUris.isNotEmpty()){
            startActivity(
                Intent(requireActivity(), CroppingActivity::class.java)
                    .putParcelableArrayListExtra(IntentIdentifiers.SELECTED_IMAGE_URI_STRINGS, ArrayList(selectedUris))
            )
            ActivityTransitions.PROCEED(requireContext())
        }
    }
}