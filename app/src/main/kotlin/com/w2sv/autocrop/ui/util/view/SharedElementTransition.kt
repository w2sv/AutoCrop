package com.w2sv.autocrop.ui.util.view

import android.animation.TimeInterpolator
import android.content.Context
import android.view.animation.Interpolator
import androidx.annotation.TransitionRes
import androidx.transition.Transition
import com.facebook.rebound.Spring
import com.facebook.rebound.SpringConfig
import com.facebook.rebound.SpringSystem
import com.w2sv.kotlinutils.threadUnsafeLazy

enum class SharedElementTransitionState {
    Entering,
    Idle,
    Exiting;

    val isIdle get() = this == Idle
}

fun inflateSharedElementTransition(
    context: Context,
    @TransitionRes resource: Int = android.R.transition.move,
    duration: Long = 900,
    interpolator: TimeInterpolator = SpringInterpolator(tension = 5.0, friction = 4.0)
): Transition? =
    inflateTransition(context, resource)?.apply {
        this.duration = duration
        this.interpolator = interpolator
    }

/**
 * A spring-based interpolator using Facebook's Rebound library.
 *
 * @param tension the tension value for the spring (Origami standard)
 *                 - Range: 0.0 (very loose) to 200.0 (very stiff)
 *                 - Recommended: 40.0-70.0 for most use cases
 *
 * @param friction the friction/damping value for the spring (Origami standard)
 *                  - Range: 1.0 (very bouncy) to 30.0 (no bounce)
 *                  - Recommended: 3.0-15.0 for most use cases
 *
 * Common presets:
 * - Very bouncy: (40.0, 3.0)
 * - Normal spring: (50.0, 7.0)
 * - Subtle spring: (60.0, 12.0)
 * - No bounce: (70.0, 20.0)
 *
 * Based on Facebook's Origami design tool parameters.
 * [link](https://facebookarchive.github.io/rebound/)
 */
class SpringInterpolator(
    tension: Double,
    friction: Double
) : Interpolator {

    private val spring: Spring by threadUnsafeLazy {
        SpringSystem
            .create()
            .createSpring().apply {
                springConfig = SpringConfig.fromOrigamiTensionAndFriction(tension, friction)
            }
    }

    override fun getInterpolation(input: Float): Float {
        // Only update endValue when we reach the start or end
        if (input == 0f || input == 1f) {
            val endValue = 1.0 - input.toDouble()
            if (spring.endValue != endValue) {
                spring.endValue = endValue
            }
        }
        return spring.currentValue.toFloat()
    }
}
