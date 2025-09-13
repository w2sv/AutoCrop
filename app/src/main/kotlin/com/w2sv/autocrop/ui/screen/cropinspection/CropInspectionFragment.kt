package com.w2sv.autocrop.ui.screen.cropinspection

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.w2sv.autocrop.ui.screen.cropSessionInjectedViewModel
import com.w2sv.autocrop.util.ComposeFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.collections.immutable.toImmutableList

@AndroidEntryPoint
class CropInspectionFragment : ComposeFragment() {

    private val viewModel by cropSessionInjectedViewModel<CropInspectionViewModel, CropInspectionViewModel.Factory>()

    @Composable
    override fun ScreenContent() {
        val context = LocalContext.current
        val deleteScreenshots by viewModel.deleteScreenshots.collectAsStateWithLifecycle()
        val cropBundles by viewModel.cropBundles.collectAsStateWithLifecycle()

        CropInspectionScreen(
            cropBundles = cropBundles.toImmutableList(),
            discardCropBundleAt = { viewModel.discardCropBundleAt(it) },
            processCropBundleAt = { viewModel.processCropBundleAt(it, context) },
            deleteScreenshots = { deleteScreenshots },
            toggleDeleteScreenshots = { viewModel.toggleDeleteScreenshots() }
        )
    }
}
