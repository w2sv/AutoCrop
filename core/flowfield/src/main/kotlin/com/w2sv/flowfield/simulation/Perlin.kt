package com.w2sv.flowfield.simulation

import kotlin.math.floor

internal object ProcessingNoise {
    private const val perlin_octaves = 4
    private const val perlin_amp_falloff = 0.5f

    // Extended permutation table (512 elements to avoid bounds issues)
    private val PERMUTATION = intArrayOf(
        151, 160, 137, 91, 90, 15, 131, 13, 201, 95, 96, 53, 194, 233, 7, 225, 140, 36, 103, 30, 69, 142, 8, 99, 37, 240, 21, 10,
        23, 190, 6, 148, 247, 120, 234, 75, 0, 26, 197, 62, 94, 252, 219, 203, 117, 35, 11, 32, 57, 177, 33, 88, 237, 149, 56, 87,
        174, 20, 125, 136, 171, 168, 68, 175, 74, 165, 71, 134, 139, 48, 27, 166, 77, 146, 158, 231, 83, 111, 229, 122, 60, 211,
        133, 230, 220, 105, 92, 41, 55, 46, 245, 40, 244, 102, 143, 54, 65, 25, 63, 161, 1, 216, 80, 73, 209, 76, 132, 187, 208,
        89, 18, 169, 200, 196, 135, 130, 116, 188, 159, 86, 164, 100, 109, 198, 173, 186, 3, 64, 52, 217, 226, 250, 124, 123, 5,
        202, 38, 147, 118, 126, 255, 82, 85, 212, 207, 206, 59, 227, 47, 16, 58, 17, 182, 189, 28, 42, 223, 183, 170, 213, 119,
        248, 152, 2, 44, 154, 163, 70, 221, 153, 101, 155, 167, 43, 172, 9, 129, 22, 39, 253, 19, 98, 108, 110, 79, 113, 224, 232,
        178, 185, 112, 104, 218, 246, 97, 228, 251, 34, 242, 193, 238, 210, 144, 12, 191, 179, 162, 241, 81, 51, 145, 235, 249, 14,
        239, 107, 49, 192, 214, 31, 181, 199, 106, 157, 184, 84, 204, 176, 115, 121, 50, 45, 127, 4, 150, 254, 138, 236, 205, 93,
        222, 114, 67, 29, 24, 72, 243, 141, 128, 195, 78, 66, 215, 61, 156, 180,
        // Repeat the permutation to avoid bounds checking
        151, 160, 137, 91, 90, 15, 131, 13, 201, 95, 96, 53, 194, 233, 7, 225, 140, 36, 103, 30, 69, 142, 8, 99, 37, 240, 21, 10,
        23, 190, 6, 148, 247, 120, 234, 75, 0, 26, 197, 62, 94, 252, 219, 203, 117, 35, 11, 32, 57, 177, 33, 88, 237, 149, 56, 87,
        174, 20, 125, 136, 171, 168, 68, 175, 74, 165, 71, 134, 139, 48, 27, 166, 77, 146, 158, 231, 83, 111, 229, 122, 60, 211,
        133, 230, 220, 105, 92, 41, 55, 46, 245, 40, 244, 102, 143, 54, 65, 25, 63, 161, 1, 216, 80, 73, 209, 76, 132, 187, 208,
        89, 18, 169, 200, 196, 135, 130, 116, 188, 159, 86, 164, 100, 109, 198, 173, 186, 3, 64, 52, 217, 226, 250, 124, 123, 5,
        202, 38, 147, 118, 126, 255, 82, 85, 212, 207, 206, 59, 227, 47, 16, 58, 17, 182, 189, 28, 42, 223, 183, 170, 213, 119,
        248, 152, 2, 44, 154, 163, 70, 221, 153, 101, 155, 167, 43, 172, 9, 129, 22, 39, 253, 19, 98, 108, 110, 79, 113, 224, 232,
        178, 185, 112, 104, 218, 246, 97, 228, 251, 34, 242, 193, 238, 210, 144, 12, 191, 179, 162, 241, 81, 51, 145, 235, 249, 14,
        239, 107, 49, 192, 214, 31, 181, 199, 106, 157, 184, 84, 204, 176, 115, 121, 50, 45, 127, 4, 150, 254, 138, 236, 205, 93,
        222, 114, 67, 29, 24, 72, 243, 141, 128, 195, 78, 66, 215, 61, 156, 180
    )

    /**
     * Processing-style noise function
     * Returns values in range [0, 1]
     */
    fun noise(x: Float, y: Float = 0f, z: Float = 0f): Float {
        var total = 0f
        var frequency = 1f
        var amplitude = 1f
        var maxValue = 0f

        repeat(perlin_octaves) {
            total += rawNoise(x * frequency, y * frequency, z * frequency) * amplitude
            maxValue += amplitude
            amplitude *= perlin_amp_falloff
            frequency *= 2f
        }

        return total / maxValue
    }

    /**
     * Raw Perlin noise function with proper bounds checking
     */
    private fun rawNoise(x: Float, y: Float, z: Float): Float {
        val X = floor(x).toInt() and 255
        val Y = floor(y).toInt() and 255
        val Z = floor(z).toInt() and 255

        val xf = x - floor(x)
        val yf = y - floor(y)
        val zf = z - floor(z)

        val u = fade(xf)
        val v = fade(yf)
        val w = fade(zf)

        // Use modulo to ensure we stay within bounds
        val A  = PERMUTATION[X] + Y
        val AA = PERMUTATION[A and 511] + Z  // Use 511 for modulo 512
        val AB = PERMUTATION[(A + 1) and 511] + Z
        val B  = PERMUTATION[(X + 1) and 255] + Y
        val BA = PERMUTATION[B and 511] + Z
        val BB = PERMUTATION[(B + 1) and 511] + Z

        val x1 = lerp(u,
            lerp(v,
                lerp(w, grad(PERMUTATION[AA and 511], xf, yf, zf),
                    grad(PERMUTATION[BA and 511], xf - 1, yf, zf)),
                lerp(w, grad(PERMUTATION[AB and 511], xf, yf - 1, zf),
                    grad(PERMUTATION[BB and 511], xf - 1, yf - 1, zf))),
            lerp(v,
                lerp(w, grad(PERMUTATION[(AA + 1) and 511], xf, yf, zf - 1),
                    grad(PERMUTATION[(BA + 1) and 511], xf - 1, yf, zf - 1)),
                lerp(w, grad(PERMUTATION[(AB + 1) and 511], xf, yf - 1, zf - 1),
                    grad(PERMUTATION[(BB + 1) and 511], xf - 1, yf - 1, zf - 1)))
        )

        return (x1 + 1f) * 0.5f // Convert to [0,1] range
    }

    private fun fade(t: Float): Float = t * t * t * (t * (t * 6 - 15) + 10)

    private fun lerp(amount: Float, left: Float, right: Float): Float =
        (1 - amount) * left + amount * right

    private fun grad(hash: Int, x: Float, y: Float, z: Float): Float {
        val h = hash and 15
        val u = if (h < 8) x else y
        val v = if (h < 4) y else if (h == 12 || h == 14) x else z
        return (if (h and 1 == 0) u else -u) + (if (h and 2 == 0) v else -v)
    }
}
