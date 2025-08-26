package com.w2sv.autocrop.ui.screen.home.compose

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.w2sv.autocrop.ui.screen.home.HomeScreenFragmentDirections
import com.w2sv.core.common.R.string as Strings

@Composable
fun HomeScreen(navController: NavController, modifier: Modifier = Modifier) {
    val launchImageSelection = rememberLaunchImageSelection { uris ->
        navController.navigate(HomeScreenFragmentDirections.navigateToCropScreen(uris.toTypedArray()))
    }

    Scaffold(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            FlowField(modifier = Modifier.fillMaxSize())
            Foreground(
                onSelectScreenshotsButtonClick = launchImageSelection,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
            )
        }
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
private fun Foreground(onSelectScreenshotsButtonClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        OutlinedButton(
            onClick = onSelectScreenshotsButtonClick,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            shape = ShapeDefaults.ExtraLarge,
            modifier = Modifier
                .width(140.dp)
                .aspectRatio(1f),
            border = BorderStroke(2.dp, Color.White),
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
