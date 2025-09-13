package com.w2sv.autocrop.ui.screen.cropinspection

import android.os.Bundle
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.w2sv.autocrop.ui.ComposeAppFragment
import com.w2sv.autocrop.ui.screen.cropSessionInjectedViewModel
import com.w2sv.autocrop.ui.util.postponeEnterTransition
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.collections.immutable.toImmutableList

@AndroidEntryPoint
class CropInspectionFragment : ComposeAppFragment() {

    private val viewModel by cropSessionInjectedViewModel<CropInspectionViewModel, CropInspectionViewModel.Factory>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        postponeEnterTransition(view)
    }

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
