package com.w2sv.autocrop.ui.screen.crop

import android.os.Bundle
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.w2sv.autocrop.R
import com.w2sv.autocrop.ui.ComposeAppFragment
import com.w2sv.autocrop.ui.screen.cropSessionInjectedViewModel
import com.w2sv.autocrop.ui.theme.AppTheme
import com.w2sv.autocrop.ui.util.compose.contentTransform
import com.w2sv.autocrop.ui.util.navigateAnimatedAndPopCurrentDestination
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CropFragment : ComposeAppFragment() {

    private val viewModel by cropSessionInjectedViewModel<CropViewModel, CropViewModel.Factory>()

    @Composable
    override fun ScreenContent() {
        val progress by viewModel.screenState.collectAsStateWithLifecycle()

        CropScreen(
            state = progress,
            onAnythingSuccessfullyCropped = {
                navController.navigateAnimatedAndPopCurrentDestination(
                    CropFragmentDirections.navigateToCropPagerScreen()
                )
            },
            onNoSuccessfulCrops = {
                navController.navigateAnimatedAndPopCurrentDestination(
                    CropFragmentDirections.navigateToCroppingFailedScreen()
                )
            }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.cropScreenshots(contentResolver = requireContext().contentResolver)
            }
        }
    }
}

@Composable
private fun CropScreen(
    state: CropScreenState,
    onAnythingSuccessfullyCropped: () -> Unit,
    onNoSuccessfulCrops: () -> Unit
) {
    state.NavigateWhenCroppingFinished(
        delay = 1000,
        onAnySuccessfulCrops = onAnythingSuccessfullyCropped,
        onNoSuccessfulCrops = onNoSuccessfulCrops
    )

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                stringResource(com.w2sv.core.common.R.string.cropping_dot_dot_dot),
                style = MaterialTheme.typography.headlineMediumEmphasized,
            )
            ScreenCenterContent(content = state.centerContent)
            Text(text = remember(state) { "${state.croppedCount}/${state.totalImageCount}" })
        }
    }
}

private val resultIconSize = 72.dp

@Composable
private fun ScreenCenterContent(content: CropScreenState.CenterContent, modifier: Modifier = Modifier) {
    AnimatedContent(
        targetState = content,
        contentAlignment = Alignment.Center,
        transitionSpec = {
            contentTransform(
                fadeIn(animationSpec = tween(500)) +
                    scaleIn(
                        animationSpec = tween(
                            durationMillis = 800,
                            easing = Easing { fraction -> OvershootInterpolator(3f).getInterpolation(fraction) }
                        )
                    ),
                fadeOut(animationSpec = tween(300)) +
                    scaleOut(animationSpec = tween(300))
            )
        },
        modifier = modifier
    ) {
        when (it) {
            CropScreenState.CenterContent.ProgressIndicator -> CircularWavyProgressIndicator(modifier = Modifier.size(92.dp))

            CropScreenState.CenterContent.SuccessIcon -> Icon(
                painterResource(R.drawable.ic_check_24),
                contentDescription = null,
                tint = Color.Green,
                modifier = Modifier.size(resultIconSize)
            )

            CropScreenState.CenterContent.ErrorIcon -> Icon(
                painterResource(com.w2sv.core.common.R.drawable.ic_error_24),
                contentDescription = null,
                modifier = Modifier.size(resultIconSize)
            )
        }
    }
}

@Preview
@Composable
private fun Prev() {
    AppTheme {
        CropScreen(
            state = CropScreenState(7, 7, true),
            {},
            {}
        )
    }
}
