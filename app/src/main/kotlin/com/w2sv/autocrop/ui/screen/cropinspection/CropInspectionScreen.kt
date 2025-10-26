package com.w2sv.autocrop.ui.screen.cropinspection

import android.view.View
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsIgnoringVisibility
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import com.w2sv.autocrop.R
import com.w2sv.autocrop.ui.screen.cropinspection.components.CropPager
import com.w2sv.autocrop.ui.screen.cropinspection.components.ProcessCropBundleDialog
import com.w2sv.autocrop.ui.theme.AppTheme
import com.w2sv.autocrop.ui.util.compose.LocalNavController
import com.w2sv.autocrop.ui.util.compose.bitmap
import com.w2sv.autocrop.ui.util.compose.debounceClick
import com.w2sv.autocrop.ui.util.compose.mockCropBundle
import com.w2sv.autocrop.ui.util.navigateAnimatedAndPopCurrentDestination
import com.w2sv.composed.OnChange
import com.w2sv.domain.model.CropBundle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.delay

@Composable
fun CropInspectionScreen(
    cropBundles: ImmutableList<CropBundle>,
    discardCropBundleAt: (Int) -> Unit,
    processCropBundleAt: (Int) -> Unit,
    deleteScreenshots: () -> Boolean,
    toggleDeleteScreenshots: () -> Unit,
    modifier: Modifier = Modifier,
    navController: NavController = LocalNavController.current
) {
    var exitAnimationPageIndex by remember { mutableStateOf<Int?>(null) }
    val pagerState = rememberPagerState { cropBundles.size }
    val pageIndication by remember { derivedStateOf { "${pagerState.currentPage + 1}/${pagerState.pageCount}" } }
    var showProcedureDialogForIndex by rememberSaveable { mutableStateOf<Int?>(null) }
    val transitionNameToImageView = remember { mutableMapOf<String, View>() }

    // Navigate to exit screen if no crop bundles left
    OnChange(cropBundles.size) {
        if (it == 0) {
            navController.navigateAnimatedAndPopCurrentDestination(CropInspectionFragmentDirections.navigateToExitScreen())
        }
    }

    // TODO: for dev only
//    LaunchedEffect(Unit) {
//        delay(500)
//        val transitionName = cropBundles[pagerState.currentPage].id
//        navController.navigate(
//            directions = CropInspectionFragmentDirections.navigateToCropAdjustmentScreen(pagerState.currentPage),
//            navigatorExtras = FragmentNavigatorExtras(transitionNameToImageView.getValue(transitionName) to transitionName)
//        )
//    }

    Scaffold(
        modifier = modifier,
        // Ignore system bars visibility so that no snapping behavior occurs during shared element transition from comparison screen,
        // during which system bars are unhidden
        contentWindowInsets = WindowInsets.systemBarsIgnoringVisibility,
        floatingActionButton = {
            ProcedureFabRow(
                onComparisonButtonClick = debounceClick {
                    val transitionName = cropBundles[pagerState.currentPage].id
                    navController.navigate(
                        directions = CropInspectionFragmentDirections.navigateToComparisonScreen(pagerState.currentPage),
                        navigatorExtras = FragmentNavigatorExtras(
                            transitionNameToImageView.getValue(transitionName) to transitionName
                        )
                    )
                },
                onAdjustButtonClick = debounceClick {
                    val transitionName = cropBundles[pagerState.currentPage].id
                    navController.navigate(
                        directions = CropInspectionFragmentDirections.navigateToCropAdjustmentScreen(pagerState.currentPage),
                        navigatorExtras = FragmentNavigatorExtras(transitionNameToImageView.getValue(transitionName) to transitionName)
                    )
                },
                onSaveButtonClick = { showProcedureDialogForIndex = pagerState.currentPage },
                onDiscardButtonClick = { exitAnimationPageIndex = pagerState.currentPage }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TopRow(
                pageIndication = pageIndication,
                onBackButtonClick = debounceClick { navController.popBackStack() },
                modifier = Modifier
                    .fillMaxHeight(0.1f)
                    .fillMaxWidth()
            )
            CropPager(
                state = pagerState,
                getCropBundle = { cropBundles[it] },
                exitAnimationPageIndex = exitAnimationPageIndex,
                onExitAnimationFinished = {
                    exitAnimationPageIndex = null
                    discardCropBundleAt(pagerState.currentPage)
                },
                onImageViewReady = { transitionName, imageView -> transitionNameToImageView[transitionName] = imageView },
                onImageViewDisposed = { transitionNameToImageView.remove(it) },
                modifier = Modifier.fillMaxHeight(0.8f)
            )
            Box(modifier = Modifier.fillMaxHeight(0.1f))
        }
    }

    showProcedureDialogForIndex?.let { index ->
        ProcessCropBundleDialog(
            deleteScreenshots = deleteScreenshots,
            toggleDeleteScreenshots = toggleDeleteScreenshots,
            onConfirmation = { processCropBundleAt(index) },
            onDismissRequest = { showProcedureDialogForIndex = null }
        )
    }
}

@Composable
private fun TopRow(
    pageIndication: String,
    onBackButtonClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onBackButtonClick) {
            Icon(painterResource(R.drawable.ic_arrow_back_24), contentDescription = null)
        }
        Text(pageIndication)
    }
}

@Composable
private fun ProcedureFabRow(
    onComparisonButtonClick: () -> Unit,
    onAdjustButtonClick: () -> Unit,
    onSaveButtonClick: () -> Unit,
    onDiscardButtonClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        ProcedureFab(onComparisonButtonClick, R.drawable.ic_inspect_image_24, stringResource(com.w2sv.core.common.R.string.compare))
        ProcedureFab(onAdjustButtonClick, R.drawable.ic_crop_24, stringResource(com.w2sv.core.common.R.string.adjust))
        ProcedureFab(onSaveButtonClick, R.drawable.ic_save_24, stringResource(com.w2sv.core.common.R.string.save))
        ProcedureFab(
            onDiscardButtonClick,
            com.w2sv.core.common.R.drawable.ic_cancel_24,
            stringResource(com.w2sv.core.common.R.string.discard)
        )
    }
}

@Composable
private fun ProcedureFab(
    onClick: () -> Unit,
    @DrawableRes drawableRes: Int,
    label: String
) {
    ExtendedFloatingActionButton(onClick = onClick) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(painterResource(drawableRes), contentDescription = null)
            Text(label)
        }
    }
}

@Preview
@Composable
private fun CropPagerScreenPrev() {
    AppTheme {
        CropInspectionScreen(
            cropBundles = persistentListOf(
                mockCropBundle(bitmap(R.drawable.mock_image))
            ),
            processCropBundleAt = {},
            discardCropBundleAt = {},
            deleteScreenshots = { true },
            toggleDeleteScreenshots = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}
