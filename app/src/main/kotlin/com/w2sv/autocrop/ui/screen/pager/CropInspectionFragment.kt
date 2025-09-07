package com.w2sv.autocrop.ui.screen.pager

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
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
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.w2sv.autocrop.R
import com.w2sv.autocrop.ui.screen.cropSessionInjectedViewModel
import com.w2sv.autocrop.ui.screen.pager.dialogs.ProcessCropBundleDialog
import com.w2sv.autocrop.ui.theme.AppTheme
import com.w2sv.autocrop.util.ComposeFragment
import com.w2sv.domain.model.Crop
import com.w2sv.domain.model.CropBundle
import com.w2sv.domain.model.CropEdges
import com.w2sv.domain.model.ImageMimeType
import com.w2sv.domain.model.Screenshot
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

@AndroidEntryPoint
class CropInspectionFragment : ComposeFragment() {

    private val viewModel by cropSessionInjectedViewModel<CropInspectionViewModel, CropInspectionViewModel.Factory>()

    @Composable
    override fun ScreenContent() {
        val context = LocalContext.current
        val deleteScreenshots by viewModel.deleteScreenshots.collectAsStateWithLifecycle()

        CropPagerScreen(
            cropBundles = viewModel.cropBundles.toImmutableList(),
            discardCropBundleAt = { viewModel.discardCropBundleAt(it) },
            processCropBundleAt = { viewModel.processCropBundleAt(it, context) },
            deleteScreenshots = { deleteScreenshots },
            toggleDeleteScreenshots = { viewModel.toggleDeleteScreenshots() }
        )
    }
}

@Composable
private fun CropPagerScreen(
    cropBundles: ImmutableList<CropBundle>,
    discardCropBundleAt: (Int) -> Unit,
    processCropBundleAt: (Int) -> Unit,
    deleteScreenshots: () -> Boolean,
    toggleDeleteScreenshots: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState { cropBundles.size }
    val pageIndication by remember { derivedStateOf { "${pagerState.currentPage + 1}/${pagerState.pageCount}" } }
    var showProcedureDialogForIndex by rememberSaveable { mutableStateOf<Int?>(null) }

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            ProcedureFabRow(
                onSaveButtonClick = { showProcedureDialogForIndex = pagerState.currentPage },
                onDiscardButtonClick = { discardCropBundleAt(pagerState.currentPage) }
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
                modifier = Modifier
                    .fillMaxHeight(0.1f)
                    .fillMaxWidth()
            )
            CropPager(state = pagerState, getCrop = { cropBundles[it].crop }, modifier = Modifier.fillMaxHeight(0.8f))
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
private fun TopRow(pageIndication: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Text(
            pageIndication,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp)
        )
    }
}

@Composable
private fun CropPager(
    state: PagerState,
    getCrop: (Int) -> Crop,
    modifier: Modifier = Modifier
) {
    HorizontalPager(
        state = state,
        modifier = modifier,
        key = { getCrop(it).hashCode() }
    ) { pageIndex ->
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Image(
                bitmap = getCrop(pageIndex).bitmap.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
private fun ProcedureFabRow(
    onSaveButtonClick: () -> Unit,
    onDiscardButtonClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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
        CropPagerScreen(
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
