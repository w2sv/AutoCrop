package com.autocrop.activities.main.fragments.flowfield.sketch;

import android.util.Pair;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import processing.core.PApplet;
import processing.core.PVector;

class FlowField extends PApplet {
    private final int cols;
    private final int rows;

    private float zOff = 0;

    final static private int FLOW_FIELD_RESOLUTION = 200;

    FlowField(int width, int height) {
        cols = floor((float) width / (float) FLOW_FIELD_RESOLUTION) + 1;
        rows = floor((float) height / (float) FLOW_FIELD_RESOLUTION) + 1;
    }

    Map<Pair<Integer, Integer>, ArrayList<Integer>> getPos2Particle(@NonNull ArrayList<Particle> particles){
        Map<Pair<Integer, Integer>, ArrayList<Integer>> pos2Particle = new HashMap<>();

        for (int i = 0; i < particles.size(); i++){
            var pos = new Pair<>(
                floor(particles.get(i).pos.x / FLOW_FIELD_RESOLUTION),
                floor(particles.get(i).pos.y / FLOW_FIELD_RESOLUTION)
            );

            if (pos2Particle.containsKey(pos))
                pos2Particle.get(pos).add(i);
            else
                pos2Particle.put(pos, new ArrayList<>(List.of(i)));
        }

        return pos2Particle;
    }

    void update(ArrayList<Particle> particles) {
        var pos2Particle = getPos2Particle(particles);

        float xOff = 0;
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                var pos = new Pair<>(x, y);
                if (pos2Particle.containsKey(pos)){
                    PVector v = PVector.fromAngle(noise(xOff, 0, zOff) * 25.132742f).normalize();

                    for (int i: pos2Particle.get(pos))
                        particles.get(i).acc.add(v);
                }
                xOff += 0.1f;
            }
        }
        zOff += 0.004;
    }
}
