package com.w2sv.autocrop.ui.screen.cropinspection

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.navigation.NavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import com.w2sv.autocrop.R
import com.w2sv.autocrop.ui.designsystem.navigateAnimatedAndPopCurrentDestination
import com.w2sv.autocrop.ui.screen.comparison.ComparisonFragment
import com.w2sv.autocrop.ui.screen.cropinspection.dialogs.ProcessCropBundleDialog
import com.w2sv.autocrop.ui.theme.AppTheme
import com.w2sv.autocrop.ui.util.compose.LocalNavController
import com.w2sv.autocrop.ui.util.compose.OnExitAnimationFinished
import com.w2sv.composed.OnChange
import com.w2sv.domain.model.Crop
import com.w2sv.domain.model.CropBundle
import com.w2sv.domain.model.CropEdges
import com.w2sv.domain.model.ImageMimeType
import com.w2sv.domain.model.Screenshot
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import slimber.log.i

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
    var imageView = remember<ImageView?> { null }

    OnChange(cropBundles.size) {
        if (it == 0) {
            navController.navigateAnimatedAndPopCurrentDestination(CropInspectionFragmentDirections.navigateToExitScreen())
        }
    }

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            ProcedureFabRow(
                onComparisonButtonClick = {
                    navController.navigate(
                        CropInspectionFragmentDirections.navigateToComparisonScreen(cropBundles[pagerState.currentPage]),
                        FragmentNavigatorExtras(
                            requireNotNull(imageView) to ComparisonFragment.TRANSITION_NAME
                        )
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
                onBackButtonClick = { navController.popBackStack() },
                modifier = Modifier
                    .fillMaxHeight(0.1f)
                    .fillMaxWidth()
            )
            CropPager(
                state = pagerState,
                getCrop = { cropBundles[it].crop },
                exitAnimationPageIndex = exitAnimationPageIndex,
                onExitAnimationFinished = {
                    i { "Calling onExitAnimationFinished" }
                    exitAnimationPageIndex = null
                    discardCropBundleAt(pagerState.currentPage)
                },
                onImageViewReady = { imageView = it },
                modifier = Modifier.fillMaxHeight(0.8f)
            )
            Box(modifier = Modifier.fillMaxHeight(0.1f))
        }
    }

    showProcedureDialogForIndex?.let { index ->
        ProcessCropBundleDialog(
            deleteScreenshots = deleteScreenshots,
            toggleDeleteScreenshots = toggleDeleteScreenshots,
            onConfirmation = {
                processCropBundleAt(index)
            },
            onDismissRequest = { showProcedureDialogForIndex = null }
        )
    }
}

@Composable
private fun TopRow(pageIndication: String, onBackButtonClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onBackButtonClick) {
            Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = null)
        }
        Text(pageIndication)
    }
}

private const val exitAnimationDuration = 500

@Composable
private fun CropPager(
    state: PagerState,
    getCrop: (Int) -> Crop,
    exitAnimationPageIndex: Int?,
    onExitAnimationFinished: () -> Unit,
    onImageViewReady: (ImageView) -> Unit,
    modifier: Modifier = Modifier
) {
    HorizontalPager(
        state = state,
        modifier = modifier,
        key = { getCrop(it).hashCode() }
    ) { pageIndex ->
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            AnimatedVisibility(
                visible = exitAnimationPageIndex != pageIndex,
                enter = EnterTransition.None,
                exit = shrinkOut(animationSpec = tween(durationMillis = exitAnimationDuration), shrinkTowards = Alignment.Center) + fadeOut(
                    animationSpec = tween(durationMillis = exitAnimationDuration)
                )
            ) {
                OnExitAnimationFinished(onExitAnimationFinished)
                SharedElementImage(
                    bitmap = getCrop(pageIndex).bitmap,
                    transitionName = ComparisonFragment.TRANSITION_NAME,
                    onImageViewReady = onImageViewReady
                )
            }
        }
    }
}

@Composable
private fun SharedElementImage(
    bitmap: Bitmap,
    transitionName: String,
    onImageViewReady: (ImageView) -> Unit,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { context ->
            ImageView(context).apply {
                setImageBitmap(bitmap)
                scaleType = ImageView.ScaleType.FIT_CENTER
                this.transitionName = transitionName
                onImageViewReady(this)
            }
        },
        modifier = modifier
    )
}

@Composable
private fun ProcedureFabRow(
    onComparisonButtonClick: () -> Unit,
    onSaveButtonClick: () -> Unit,
    onDiscardButtonClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        ProcedureFab(onComparisonButtonClick, R.drawable.ic_inspect_image_24, LocalContentColor.current, "Compare")
        ProcedureFab(onSaveButtonClick, R.drawable.ic_save_24, Color.Green, stringResource(com.w2sv.core.common.R.string.save))
        ProcedureFab(
            onDiscardButtonClick,
            com.w2sv.core.common.R.drawable.ic_cancel_24,
            Color.Red,
            stringResource(com.w2sv.core.common.R.string.discard)
        )
    }
}

@Composable
private fun ProcedureFab(
    onClick: () -> Unit,
    @DrawableRes drawableRes: Int,
    drawableTint: Color,
    label: String
) {
    ExtendedFloatingActionButton(onClick = onClick) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(painterResource(drawableRes), contentDescription = null, tint = drawableTint)
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

private fun mockCropBundle(bitmap: Bitmap): CropBundle =
    CropBundle(
        Screenshot(
            "".toUri(),
            0,
            Screenshot.MediaStoreData(0L, "", ImageMimeType.JPG, 0L)
        ),
        Crop(
            bitmap,
            CropEdges(0, 0),
            -1,
            0L
        ),
        listOf(),
        0
    )

@Composable
private fun bitmap(@DrawableRes res: Int): Bitmap {
    val resources = LocalResources.current
    return BitmapFactory.decodeResource(
        resources,
        res
    )
}
