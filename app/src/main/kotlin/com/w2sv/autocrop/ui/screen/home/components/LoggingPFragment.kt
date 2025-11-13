package com.w2sv.autocrop.ui.screen.home.components

import android.content.res.Configuration
import android.graphics.Point
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.fragment.app.FragmentActivity
import processing.android.CompatUtils
import processing.android.PFragment
import processing.core.PApplet
import slimber.log.i

class LoggingPFragment : PFragment {
    private var metrics: DisplayMetrics? = null
    private var size: Point? = null
    private var sketch: PApplet? = null

    @LayoutRes
    private var layout = -1

    constructor()

    constructor(sketch: PApplet) {
        this.setSketch(sketch)
    }

    override fun initDimensions() {
        this.metrics = DisplayMetrics()
        this.size = Point()
        val wm = requireActivity().windowManager
        val display = wm.defaultDisplay
        CompatUtils.getDisplayParams(display, this.metrics, this.size)
    }

    override fun getDisplayWidth(): Int {
        return this.size!!.x
    }

    override fun getDisplayHeight(): Int {
        return this.size!!.y
    }

    override fun getDisplayDensity(): Float {
        return this.metrics!!.density
    }

    override fun setSketch(sketch: PApplet) {
        this.sketch = sketch
        if (this.layout != -1) {
            sketch.parentLayout = this.layout
        }
    }

    override fun getSketch(): PApplet? {
        return this.sketch
    }

    override fun setView(view: View, activity: FragmentActivity) {
        i { "setView sketch=$sketch" }
        val manager = activity.supportFragmentManager
        val transaction = manager.beginTransaction()
        transaction.add(view.id, this)
        transaction.commit()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        i { "onCreateView sketch=$sketch" }
        if (this.sketch != null) {
            this.sketch!!.initSurface(inflater, container, savedInstanceState, this, null)
            this.sketch!!.onCreate(savedInstanceState)
            return this.sketch!!.getSurface().rootView
        }
        else {
            return null
        }
    }

    override fun onStart() {
        i { "onStart sketch=$sketch" }
        super.onStart()
        if (this.sketch != null) {
            this.sketch!!.onStart()
        }
    }

    override fun onResume() {
        i { "onResume sketch=$sketch" }
        super.onResume()
        if (this.sketch != null) {
            this.sketch!!.onResume()
        }
    }

    override fun onPause() {
        i { "onPause sketch=$sketch" }
        super.onPause()
        if (this.sketch != null) {
            this.sketch!!.onPause()
        }
    }

    override fun onStop() {
        i { "onStop sketch=$sketch" }
        super.onStop()
        if (this.sketch != null) {
            this.sketch!!.onStop()
        }
    }

    override fun onDestroy() {
        i { "onDestroy sketch=$sketch" }
        super.onDestroy()
        if (this.sketch != null) {
            this.sketch!!.onDestroy()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    override fun setOrientation(which: Int) {
        i { "setOrientation $which" }
        if (which == 1) {
            this.requireActivity().setRequestedOrientation(1)
        }
        else if (which == 2) {
            this.requireActivity().setRequestedOrientation(0)
        }
    }

    override fun canDraw(): Boolean {
        return this.sketch != null && this.sketch!!.isLooping
    }
}
