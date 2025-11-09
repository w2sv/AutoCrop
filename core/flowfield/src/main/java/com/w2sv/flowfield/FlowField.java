package com.w2sv.flowfield;

import static processing.core.PConstants.TWO_PI;

import java.util.HashMap;
import java.util.Map;

import processing.core.PApplet;
import processing.core.PVector;

class FlowField {
    private static final float NOISE_ANGLE_SCALE = TWO_PI * 4; // 4 full rotations
    private final int granularity;
    private final float zOffIncrement;
    private float zOff = 0;

    private final Map<Long, Float> xNoiseCache = new HashMap<>();

    FlowField(int granularity, float zOffIncrement) {
        this.granularity = granularity;
        this.zOffIncrement = zOffIncrement;
    }

    void prepareFrame() {
        // Increment z-offset for temporal evolution of the field
        zOff += zOffIncrement;
    }

    float forceAngle(PVector pos, PApplet parent) {
        // Map particle position to grid cell coordinates
        int xCell = PApplet.floor(pos.x / granularity) + 1;
        int yCell = PApplet.floor(pos.y / granularity) + 1;
        // Pack cell coordinates into a long key for caching
        long key = packedLong(xCell, yCell);
        // Compute a unique x-offset for Perlin noise (cached per cell)
        float xNoise = xNoiseCache.computeIfAbsent(key, kk -> noiseXOffset(xCell, yCell, 0.1f));
        // Compute angle from noise
        return parent.noise(xNoise, 0, zOff) * NOISE_ANGLE_SCALE;
    }

    /**
     * Packs two ints into a single long (high 32 bits = a, low 32 bits = b).
     */
    private static long packedLong(int a, int b) {
        return (((long) a) << 32) | (b & 0xFFFFFFFFL);
    }

    /**
     * Maps a 2D grid cell (gx, gy) to a unique float suitable for Perlin noise input.
     *
     * <p>
     * Uses a pairing function (inspired by the Cantor pairing function) to ensure
     * each integer grid cell maps to a distinct value, while scaling it to keep
     * noise smooth.
     * </p>
     *
     * @param x     the x-index of the grid cell
     * @param y     the y-index of the grid cell
     * @param scale a scaling factor to control noise frequency (e.g., 0.1f)
     * @return a unique float representing the cell for use in noise()
     */
    private static float noiseXOffset(int x, int y, float scale) {
        return (((x + y) * (x + y + 1) / 2.0f + y) * scale);
    }
}
