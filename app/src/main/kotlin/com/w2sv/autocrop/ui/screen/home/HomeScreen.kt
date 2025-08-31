package com.w2sv.autocrop.ui.screen.home

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.FloatRange
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.w2sv.autocrop.R
import com.w2sv.autocrop.ui.screen.home.components.FlowFieldOrPreviewMock
import com.w2sv.autocrop.ui.screen.home.components.NavigationDrawer
import com.w2sv.composed.extensions.rememberVisibilityPercentage
import kotlinx.coroutines.launch
import com.w2sv.core.common.R.string as Strings

@Composable
fun HomeScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Open)
) {
    val launchImageSelection = rememberLaunchImageSelection { uris ->
        navController.navigate(HomeScreenFragmentDirections.navigateToCropScreen(uris.toTypedArray()))
    }
    val scope = rememberCoroutineScope()
    val drawerVisibilityPercentage by drawerState.rememberVisibilityPercentage()

    NavigationDrawer(state = drawerState) {
        Scaffold(
            modifier = modifier.background(Color.Black),  // For the brief moment after initializing at which the flowfield is not yet drawing
            topBar = {
                LottieButton(
                    animationProgress = { drawerVisibilityPercentage },
                    onClick = { scope.launch { drawerState.open() } },
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.statusBarsIgnoringVisibility)
                        .padding(start = 12.dp, top = 12.dp)
                        .size(46.dp)
                )
            }
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                FlowFieldOrPreviewMock(modifier = Modifier.fillMaxSize())
                Foreground(
                    alphaPercentage = remember(drawerVisibilityPercentage) { 1 - drawerVisibilityPercentage },
                    onSelectScreenshotsButtonClick = launchImageSelection,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(it)
                )
            }
        }
    }
}

@Preview
@Composable
private fun Prev() {
    HomeScreen(NavController(LocalContext.current))
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
private fun LottieButton(animationProgress: () -> Float, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.hamburger_to_backarrow))

    Box(
        modifier = modifier
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        LottieAnimation(
            composition = composition,
            progress = animationProgress
        )
    }
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
    @FloatRange(0.0, 1.0) alphaPercentage: Float,
    onSelectScreenshotsButtonClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentColor = Color.White.copy(alpha = alphaPercentage)
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
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 2
            )
        }
    }
}
