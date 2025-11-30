package com.w2sv.flowfield

internal object Config {
    const val PARTICLE_COUNT = 2_000
    const val PARTICLE_MAX_VELOCITY = 5f

    const val CELLS_PER_PIXEL = 0.005f
    const val NOISE_SAMPLING_TEMPORAL_INCREMENT = 0.005f
    const val RADIAN_SAMPLING_COEFFICIENT = 3f

    const val ALPHA_SUBTRACTION_PER_FRAME = 0.004f
}
