package com.autocrop.activities.main.fragments.flowfield

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.FragmentActivity
import com.autocrop.activities.ActivityTransitions
import com.autocrop.activities.IntentIdentifiers
import com.autocrop.activities.cropping.CroppingActivity
import com.autocrop.activities.main.MainActivity
import com.autocrop.activities.main.fragments.MainActivityFragment
import com.autocrop.global.UserPreferences
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

        // initialize UserPreferences if necessary
        if (!UserPreferences.isInitialized)
            UserPreferences.initializeFromSharedPreferences(requireActivity().getSharedPreferences())

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
                    R.id.main_menu_item_conduct_auto_scrolling to UserPreferences.Keys.conductAutoScrolling
                ).forEach { (id, userPreferencesKey) ->
                    with(menu.findItem(id)){
                        isChecked = UserPreferences.getValue(userPreferencesKey)

                        setOnMenuItemClickListener { item ->
                            UserPreferences.toggle(userPreferencesKey)
                            isChecked = !isChecked
                            item.persistMenuAfterClick(requireContext())
                        }
                    }
                }

                mapOf(
                    R.id.main_menu_item_rate_the_app to ::goToPlayStoreListing,
                    R.id.main_menu_item_about_the_app to { with(typedActivity) { hideAndShowFragments(rootFragment, aboutFragment) } }
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

    //$$$$$$$$$$$$$$
    // Permissions $
    //$$$$$$$$$$$$$$
    private inner class PermissionsHandler{
        private val missingPermissions: List<String>
            get() = REQUIRED_PERMISSIONS.filter { !requireActivity().permissionGranted(it) }

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
                requireActivity().displaySnackbar(
                    "You need to permit file reading and\nwriting in order for the app to work",
                    TextColors.NEUTRAL
                )
                Timber.i("Not all required permissions were granted; permissions: ${permissions.toList()} | grantResults: ${grantResults.toList()}")
            }
            else
                onAllPermissionsGranted!!()
            onAllPermissionsGranted = null
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray){

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
            type = IMAGE_MIME_TYPE
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            startActivityForResult(
                this,
                MainActivity.IntentCodes.IMAGE_SELECTION.ordinal
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == FragmentActivity.RESULT_OK && requestCode == MainActivity.IntentCodes.IMAGE_SELECTION.ordinal) {
            with(data?.clipData!!) {
                startCroppingActivity(imageUris = ArrayList((0 until itemCount).map { getItemAt(it)?.uri!! }))
            }
        }
    }

    //$$$$$$$$$$
    // Exiting $
    //$$$$$$$$$$
    private fun startCroppingActivity(imageUris: ArrayList<Parcelable>) {
        startActivity(
            Intent(requireActivity(), CroppingActivity::class.java)
                .putParcelableArrayListExtra(IntentIdentifiers.SELECTED_IMAGE_URI_STRINGS, imageUris)
        )
        ActivityTransitions.PROCEED(requireContext())
    }
}