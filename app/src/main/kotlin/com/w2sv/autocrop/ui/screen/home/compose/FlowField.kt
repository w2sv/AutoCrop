package com.w2sv.autocrop.ui.screen.home.compose

import android.view.View
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import com.w2sv.autocrop.ui.util.findActivity
import com.w2sv.autocrop.ui.util.rememberScreenResolution
import com.w2sv.flowfield.PerlinNoiseFlowFieldSketch
import processing.android.PFragment
import processing.core.PApplet

@Composable
fun FlowField(modifier: Modifier = Modifier) {
    val screenResolution = rememberScreenResolution()
    val sketch = remember { PerlinNoiseFlowFieldSketch(screenResolution) }
    ProcessingSketch(sketch, modifier)
}

@Composable
private fun ProcessingSketch(sketch: PApplet, modifier: Modifier = Modifier) {
    AndroidView(
        factory = { context ->
            FrameLayout(context).apply {
                id = View.generateViewId()
                PFragment(sketch)
                    .setView(
                        this,
                        context.findActivity() as? FragmentActivity
                            ?: throw IllegalStateException("FragmentActivity not found for ProcessingSketch")
                    )
            }
        },
        modifier = modifier
    )
}
