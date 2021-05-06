package com.autocrop.activities.main;

import android.graphics.Point;

import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PVector;


public class FlowFieldPApplet extends PApplet {
    final int FLOW_FIELD_RESOLUTION = 90;
    final int N_PARTICLES = 800;

    final float PARTICLE_STROKE_WEIGHT = 2;
    final int ALPHA = 18;

    final float PARTICLE_VELOCITY_LOWER_BOUND = 12;
    final float PARTICLE_VELOCITY_UPPER_BOUND = 18;

    private class FlowField {
        PVector[] vectors;
        int cols, rows;
        float inc = 0.1f;
        float zoff = 0;
        int resolution;

        FlowField(int _resolution) {
            resolution = _resolution;
            cols = floor((float)width / (float)resolution) + 1;
            rows = floor((float)height / (float)resolution) + 1;
            vectors = new PVector[cols * rows];
        }

        void update() {
            float xoff = 0;
            for (int y = 0; y < rows; y++) {
                float yoff = 0;
                for (int x = 0; x < cols; x++) {
                    float angle = noise(xoff, yoff, zoff) * TWO_PI * 4;

                    PVector v = PVector.fromAngle(angle);
                    v.setMag(1);
                    int index = x + y * cols;
                    vectors[index] = v;

                    xoff += inc;
                }
            }
            zoff += 0.004;
        }

        void affect(Particle particle){
            int x = floor(particle.pos.x / resolution);
            int y = floor(particle.pos.y / resolution);
            int index = x + y * cols;

            particle.acc.add(vectors[index]);
        }
    }

    private class Particle {
        PVector pos;
        PVector acc;
        private PVector vel;
        private PVector previousPos;
        private float maxSpeed;

        Particle() {
            maxSpeed = random(PARTICLE_VELOCITY_LOWER_BOUND, PARTICLE_VELOCITY_UPPER_BOUND);

            pos = new PVector(random(width), random(height));
            previousPos = pos.copy();

            vel = new PVector(0, 0);
            acc = new PVector(0, 0);
        }

        void update() {
            vel.limit(maxSpeed);
            pos.add(vel);
            keepWithinBounds();

            vel.add(acc);
            acc.mult(0);
        }

        private void keepWithinBounds() {
            boolean changedCoordinate = false;

            if (pos.x > width) {
                pos.x = 0;
                changedCoordinate = true;
            }
            else if (pos.x < 0) {
                pos.x = width;
                changedCoordinate = true;
            }

            if (pos.y > height) {
                pos.y = 0;
                changedCoordinate = true;
            }
            else if (pos.y < 0) {
                pos.y = height;
                changedCoordinate = true;
            }

            if (changedCoordinate)
                updatePreviousPos();
        }

        private void updatePreviousPos() {
            previousPos.x = pos.x;
            previousPos.y = pos.y;
        }

        private void show() {
            stroke(139, 0, 0, ALPHA);
            strokeWeight(PARTICLE_STROKE_WEIGHT);
            line(pos.x, pos.y, previousPos.x, previousPos.y);
            point(pos.x, pos.y);  // ?
            updatePreviousPos();
        }
    }

    FlowField flowfield;
    ArrayList<Particle> particles;

    public FlowFieldPApplet(Point screen_resolution) {
        width = screen_resolution.x;
        height = screen_resolution.y;
    }

    public void settings() {
        size(width, height, P2D);
    }

    public void setup() {
        background(0);

        flowfield = new FlowField(FLOW_FIELD_RESOLUTION);
        flowfield.update();

        particles = new ArrayList<>();
        for (int i = 0; i < N_PARTICLES; i++) {
            particles.add(new Particle());
        }
    }

    public void draw() {
        flowfield.update();

        for (Particle p : particles) {
            flowfield.affect(p);
            p.update();
            p.show();
        }
    }
}
