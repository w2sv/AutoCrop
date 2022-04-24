package com.autocrop.activities.main.fragments.flowfield.sketch;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PVector;

class FlowField extends PApplet {
    PVector[] vectors;
    int cols, rows;

    float inc = 0.1f;
    float zOff = 0;

    final static int FLOW_FIELD_RESOLUTION = 200;

    FlowField(int width, int height) {
        cols = floor((float) width / (float) FLOW_FIELD_RESOLUTION) + 1;
        rows = floor((float) height / (float) FLOW_FIELD_RESOLUTION) + 1;
        vectors = new PVector[cols * rows];
    }

    void update() {
        float xOff = 0;
        for (int y = 0; y < rows; y++) {
            float yOff = 0;
            for (int x = 0; x < cols; x++) {
                float angle = noise(xOff, yOff, zOff) * PConstants.TWO_PI * 4;

                PVector v = PVector.fromAngle(angle);
                v.setMag(1);
                int index = x + y * cols;
                vectors[index] = v;

                xOff += inc;
            }
        }
        zOff += 0.004;
    }

    void affect(Particle particle) {
        int x = PApplet.floor(particle.pos.x / FLOW_FIELD_RESOLUTION);
        int y = PApplet.floor(particle.pos.y / FLOW_FIELD_RESOLUTION);
        int index = x + y * cols;

        particle.acc.add(vectors[index]);
    }
}
