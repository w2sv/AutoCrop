package com.w2sv.autocrop.ui.screen.home

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.FloatRange
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsIgnoringVisibility
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.w2sv.autocrop.R
import com.w2sv.autocrop.ui.screen.home.components.FlowFieldOrPreviewMock
import com.w2sv.autocrop.ui.screen.home.components.NavigationDrawer
import com.w2sv.autocrop.ui.util.compose.LocalNavController
import com.w2sv.autocrop.ui.util.compose.LottieButton
import com.w2sv.autocrop.ui.util.navigateAnimated
import com.w2sv.composed.extensions.rememberVisibilityPercentage
import com.w2sv.core.common.R.string as Strings
import com.w2sv.kotlinutils.coroutines.launchDelayed
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    navController: NavController = LocalNavController.current,
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
) {
    val scope = rememberCoroutineScope()
    val launchImageSelection = rememberLaunchImageSelection { uris ->
        // Give image picker time to close so that nav animation is properly displayed
        scope.launchDelayed(200L) {
            navController.navigateAnimated(HomeScreenFragmentDirections.navigateToCropScreen(uris.toTypedArray()))
        }
    }
    val drawerVisibilityPercentage by drawerState.rememberVisibilityPercentage()

    Box(modifier = modifier.fillMaxSize()) {
        NavigationDrawer(state = drawerState) {
            Scaffold {
                FlowFieldOrPreviewMock(modifier = Modifier.fillMaxSize())
                Foreground(
                    alpha = remember(drawerVisibilityPercentage) { 1 - drawerVisibilityPercentage },
                    onSelectScreenshotsButtonClick = launchImageSelection,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(it)
                )
            }
        }
        LottieButton(
            animationRes = R.raw.hamburger_to_backarrow,
            animationProgress = { drawerVisibilityPercentage },
            onClick = { scope.launch { if (drawerState.isOpen) drawerState.close() else drawerState.open() } },
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.statusBarsIgnoringVisibility)
                .padding(start = 12.dp, top = 12.dp)
                .size(46.dp)
        )
    }
}

@Preview
@Composable
private fun Prev() {
    HomeScreen()
}

@Preview
@Composable
private fun DrawerPrev() {
    HomeScreen(
        navController = NavController(LocalContext.current),
        drawerState = DrawerState(initialValue = DrawerValue.Open)
    )
}

@Composable
private fun rememberLaunchImageSelection(onImagesSelected: (List<Uri>) -> Unit): () -> Unit {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris ->
            if (uris.isNotEmpty()) {
                onImagesSelected(uris)
            }
        }
    )

    return remember(launcher) {
        {
            launcher.launch(
                PickVisualMediaRequest(
                    ActivityResultContracts.PickVisualMedia.ImageOnly
                )
            )
        }
    }
}

@Composable
private fun Foreground(
    @FloatRange(0.0, 1.0) alpha: Float,
    onSelectScreenshotsButtonClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentColor = Color.White.copy(alpha = alpha)
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        OutlinedButton(
            onClick = onSelectScreenshotsButtonClick,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = contentColor),
            shape = ShapeDefaults.ExtraLarge,
            modifier = Modifier
                .width(140.dp)
                .aspectRatio(1f),
            border = BorderStroke(2.dp, contentColor),
            contentPadding = PaddingValues(16.dp)
        ) {
            Text(
                stringResource(Strings.select_screenshots),
                textAlign = TextAlign.Center,
                style = typography.bodyLarge,
                maxLines = 2
            )
        }
    }
}
