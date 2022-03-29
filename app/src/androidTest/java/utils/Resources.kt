package utils

import androidx.test.platform.app.InstrumentationRegistry
import java.io.InputStream

fun streamAssetFile(subPath: String): InputStream = InstrumentationRegistry.getInstrumentation().context.resources.assets.open(subPath)