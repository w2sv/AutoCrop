package com.autocrop.activities.main.fragments.flowfield

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.text.color
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout.DrawerListener
import com.autocrop.activities.cropping.CropActivity
import com.autocrop.activities.main.MainActivity
import com.autocrop.activities.main.fragments.MainActivityFragment
import com.autocrop.preferences.UriPreferences
import com.autocrop.utils.android.PermissionHandler
import com.autocrop.utils.android.documentUriPathIdentifier
import com.autocrop.utils.android.extensions.getThemedColor
import com.autocrop.utils.android.extensions.show
import com.autocrop.utils.android.extensions.snacky
import com.w2sv.autocrop.R
import com.w2sv.autocrop.databinding.FragmentFlowfieldBinding

class FlowFieldFragment:
    MainActivityFragment<FragmentFlowfieldBinding>(FragmentFlowfieldBinding::class.java) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        with(lifecycle){
            addObserver(writeExternalStoragePermissionHandler)
            addObserver(readExternalStoragePermissionHandler)
            notificationPostingPermissionHandler?.let {
                addObserver(it)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.drawerLayout.addDrawerListener(object: DrawerListener{
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                val fadeOutOnDrawerOpenAlpha = 1 - slideOffset

                binding.navigationDrawerButtonBurger.alpha = fadeOutOnDrawerOpenAlpha
                binding.navigationDrawerButtonArrow.alpha = slideOffset

                binding.imageSelectionButton.alpha = fadeOutOnDrawerOpenAlpha
            }
            override fun onDrawerOpened(drawerView: View) {}
            override fun onDrawerClosed(drawerView: View) {}
            override fun onDrawerStateChanged(newState: Int) {}
        })

        binding.navigationDrawerButtonArrow.setOnClickListener {
            with(binding.drawerLayout){
                val gravity = GravityCompat.START

                if (isOpen)
                    closeDrawer(gravity)
                else
                    openDrawer(gravity)
            }
        }
    }

    val writeExternalStoragePermissionHandler by lazy {
        PermissionHandler(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            requireActivity(),
            "Media file writing required for saving crops",
            "Go to app settings and grant media file writing in order for the app to work"
        )
    }
    val readExternalStoragePermissionHandler by lazy {
        PermissionHandler(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            requireActivity(),
            "Media file access required for listening to screen captures",
            "Go to app settings and grant media file access for screen capture listening to work"
        )
    }
    val notificationPostingPermissionHandler: PermissionHandler? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            PermissionHandler(
                Manifest.permission.POST_NOTIFICATIONS,
                requireActivity(),
                "If you don't allow for the posting of notifications AutoCrop can't inform you about croppable screenshots",
                "Go to app settings and enable notification posting for screen capture listening to work"
            )
        else
            null
    }

    val imageSelectionIntentLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
        activityResult.data?.let { intent ->
            intent.clipData?.let { clipData ->
                startActivity(
                    Intent(
                        requireActivity(),
                        CropActivity::class.java
                    )
                        .putParcelableArrayListExtra(
                            MainActivity.EXTRA_SELECTED_IMAGE_URIS,
                            ArrayList((0 until clipData.itemCount)
                                .map { clipData.getItemAt(it).uri })
                        )
                )
            }
        }
    }

    val saveDestinationSelectionIntentLauncher = registerForActivityResult(object: ActivityResultContracts.OpenDocumentTree(){
        override fun createIntent(context: Context, input: Uri?): Intent =
            super.createIntent(context, input)
                .setFlags(
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or
                    Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
                )
    }) {
        it?.let { treeUri ->
            if (UriPreferences.treeUri != treeUri){
                UriPreferences.treeUri = treeUri

                requireContext()
                    .contentResolver
                    .takePersistableUriPermission(
                        treeUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                requireActivity().snacky(
                    SpannableStringBuilder()
                        .append("Crops will be saved to ")
                        .color(requireContext().getThemedColor(R.color.notification_success)){
                            append(
                                documentUriPathIdentifier(UriPreferences.documentUri!!)
                            )
                        }
                )
                    .show()
            }
        }
    }
}