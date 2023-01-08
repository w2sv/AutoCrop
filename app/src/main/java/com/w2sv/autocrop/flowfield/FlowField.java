package com.w2sv.autocrop.flowfield;

import android.util.Pair;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import processing.core.PApplet;
import processing.core.PVector;

class FlowField extends PApplet {
    private static final Map<Pair<Integer, Integer>, Float> xOffCache = new HashMap<>();
    private float zOff = 0;

    void update(ArrayList<Particle> particles) {
        final int RESOLUTION = Sketch.Config.FLOW_FIELD_RESOLUTION;

        var pos2Noise = new HashMap<Pair<Integer, Integer>, PVector>();

        for (int i = 0; i < particles.size(); i++) {
            var pos = new Pair<>(
                    floor(particles.get(i).pos.x / RESOLUTION) + 1,
                    floor(particles.get(i).pos.y / RESOLUTION) + 1
            );

            if (pos2Noise.containsKey(pos))
                particles.get(i).applyFlowFieldVector(pos2Noise.get(pos));
            else {
                PVector v = PVector.fromAngle(noise(xOff(pos), 0, zOff) * 25.132742f).normalize();
                particles.get(i).applyFlowFieldVector(v);
                pos2Noise.put(pos, v);
            }
        }

        zOff += Sketch.Config.FLOW_FIELD_Z_OFF_INCREMENT;
    }

    private float xOff(@NonNull Pair<Integer, Integer> pos) {
        if (xOffCache.containsKey(pos))
            //noinspection ConstantConditions
            return xOffCache.get(pos);
        float xOff = pos.first * pos.second * 0.1f;
        xOffCache.put(pos, xOff);
        return xOff;
    }
}
