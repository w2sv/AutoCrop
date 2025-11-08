package com.w2sv.autocrop.ui.util.compose

import android.view.View
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.commitNow
import com.w2sv.autocrop.ui.util.findActivity
import java.util.UUID
import processing.android.PFragment
import processing.core.PApplet

@Composable
fun ProcessingSketch(sketch: PApplet, modifier: Modifier = Modifier) {
    val fragmentTag = rememberSaveable { "PFragment_${UUID.randomUUID()}" }

    AndroidView(
        factory = { context -> FrameLayout(context).apply { id = View.generateViewId() } },
        update = { view ->
            val activity = view.context.findActivity() as? FragmentActivity
                ?: error("FragmentActivity not found for ProcessingSketch")
            val fragmentManager = activity.supportFragmentManager

            fragmentManager.commitNow(allowStateLoss = true) {
                // Remove existing fragment, if present
                fragmentManager.findFragmentByTag(fragmentTag)?.let { existingFragment ->
                    remove(existingFragment)
                }
                // Add new fragment
                val fragment = PFragment(sketch)
                replace(view.id, fragment, fragmentTag)
            }
        },
        modifier = modifier
    )
}
