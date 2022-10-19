package com.autocrop.activities.main.fragments.flowfield.sketch;

import android.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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

    void update(ArrayList<Particle> particles) {
        Map<Pair<Integer, Integer>, Particle> flowFieldPos2Particle = new Defa<>();
        for (Particle p: particles){
            int x = PApplet.floor(p.pos.x / FLOW_FIELD_RESOLUTION);
            int y = PApplet.floor(p.pos.y / FLOW_FIELD_RESOLUTION);

            flowFieldPos2Particle.put(new Pair<>(x, y), p);
        }

        float xOff = 0;
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                var pos = new Pair<>(x, y);
                if (flowFieldPos2Particle.containsKey(pos)){
                    PVector v = PVector.fromAngle(noise(xOff, 0, zOff) * 25.132742f);
                    v.setMag(1);
                    flowFieldPos2Particle.get(pos).acc.add(v);
                }
                xOff += 0.1f;
            }
        }
        zOff += 0.004;
    }
}
