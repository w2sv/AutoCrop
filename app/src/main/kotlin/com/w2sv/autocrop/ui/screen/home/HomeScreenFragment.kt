package com.w2sv.autocrop.ui.screen.home

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.w2sv.autocrop.ui.ComposeAppFragment
import com.w2sv.autocrop.ui.util.navigateAnimated
import com.w2sv.kotlinutils.coroutines.launchDelayed
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeScreenFragment : ComposeAppFragment() {

    @Composable
    override fun ScreenContent() {
        val scope = rememberCoroutineScope()
        val launchImageSelection = rememberLaunchImageSelection { uris ->
            // Give image picker time to close so that nav animation is properly displayed
            scope.launchDelayed(200L) {
                navController.navigateAnimated(HomeScreenFragmentDirections.navigateToCropScreen(uris.toTypedArray()))
            }
        }
        HomeScreen(launchImageSelection = launchImageSelection)
    }
}

@Composable
fun rememberLaunchImageSelection(onImagesSelected: (List<Uri>) -> Unit): () -> Unit {
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
