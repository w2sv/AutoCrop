package com.w2sv.autocrop.ui.util.compose

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.w2sv.domain.model.Crop
import com.w2sv.domain.model.CropBundle
import com.w2sv.domain.model.CropEdges
import com.w2sv.domain.model.ImageMimeType
import com.w2sv.domain.model.Screenshot

fun mockCropBundle(bitmap: Bitmap): CropBundle =
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
fun bitmap(@DrawableRes res: Int): Bitmap {
    val resources = LocalResources.current
    return BitmapFactory.decodeResource(
        resources,
        res
    )
}

@Composable
fun mockNavController(): NavController =
    NavController(LocalContext.current)
