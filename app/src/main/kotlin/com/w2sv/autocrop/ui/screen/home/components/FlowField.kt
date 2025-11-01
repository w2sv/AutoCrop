package com.w2sv.autocrop.ui.screen.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import com.w2sv.autocrop.ui.util.compose.ProcessingSketch
import com.w2sv.autocrop.ui.util.rememberScreenResolution
import com.w2sv.flowfield.PerlinNoiseFlowFieldSketch

@Composable
fun FlowFieldOrPreviewMock(modifier: Modifier = Modifier) {
    if (LocalInspectionMode.current) {
        Box(
            modifier
                .background(Color(145, 25, 69))
        )
    }
    else {
        FlowField(modifier)
    }
}

@Composable
private fun FlowField(modifier: Modifier = Modifier) {
    val screenResolution = rememberScreenResolution()
    val sketch = remember(screenResolution) { PerlinNoiseFlowFieldSketch(screenResolution.x, screenResolution.y) }
    ProcessingSketch(sketch, modifier)
}
