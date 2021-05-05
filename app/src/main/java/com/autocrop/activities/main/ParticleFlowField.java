package com.autocrop.activities.main;

import android.graphics.Point;

import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PVector;


public class ParticleFlowField extends PApplet {
    private class FlowField {
        PVector[] vectors;
        int cols, rows;
        float inc = 0.1f;
        float zoff = 0;
        int resolution;

        FlowField(int _resolution) {
            resolution = _resolution;
            cols = floor((float)width / (float)_resolution) + 1;
            rows = floor((float)height / (float)_resolution) + 1;
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
                yoff += inc;
            }
            zoff += 0.004;
        }

        void display() {
            for (int y = 0; y < rows; y++) {
                for (int x = 0; x < cols; x++) {
                    int index = x + y * cols;
                    PVector v = vectors[index];

                    stroke(0, 0, 0, 40);
                    strokeWeight(30);
                    pushMatrix();
                    translate(x * resolution, y * resolution);
                    rotate(v.heading());
                    line(0, 0, resolution, 0);
                    popMatrix();
                }
            }
        }
    }

    private class Particle {
        PVector pos;
        PVector vel;
        PVector acc;
        PVector previousPos;
        float maxSpeed;

        Particle(PVector start, float maxspeed) {
            maxSpeed = maxspeed;
            pos = start;
            vel = new PVector(0, 0);
            acc = new PVector(0, 0);
            previousPos = pos.copy();
        }

        void run() {
            update();
            edges();
            show();
        }

        void update() {
            pos.add(vel);
            vel.limit(maxSpeed);
            vel.add(acc);
            acc.mult(0);
        }

        void applyForce(PVector force) {
            acc.add(force);
        }

        void show() {
            stroke(139, 0, 0, 5); // first -> color, second -> alpha; 139
            strokeWeight(5);
            line(pos.x, pos.y, previousPos.x, previousPos.y);
            //point(pos.x, pos.y);
            updatePreviousPos();
        }

        void edges() {
            if (pos.x > width) {
                pos.x = 0;
                updatePreviousPos();
            }
            if (pos.x < 0) {
                pos.x = width;
                updatePreviousPos();
            }
            if (pos.y > height) {
                pos.y = 0;
                updatePreviousPos();
            }
            if (pos.y < 0) {
                pos.y = height;
                updatePreviousPos();
            }
        }

        void updatePreviousPos() {
            previousPos.x = pos.x;
            previousPos.y = pos.y;
        }

        void follow(FlowField flowfield) {
            int x = floor(pos.x / flowfield.resolution);
            int y = floor(pos.y / flowfield.resolution);
            int index = x + y * flowfield.cols;

            PVector force = flowfield.vectors[index];
            applyForce(force);
        }
    }

    FlowField flowfield;
    ArrayList<Particle> particles;

    boolean debug = false;

    public ParticleFlowField(Point screen_resolution) {
        width = screen_resolution.x;
        height = screen_resolution.y;
    }

    public void settings() {
        size(width, height, P2D);
    }

    public void setup() {
        background(0);

        final int FLOW_FIELD_RESOLUTION = 90;
        final int N_PARTICLES = 800;

        flowfield = new FlowField(FLOW_FIELD_RESOLUTION);
        flowfield.update();

        particles = new ArrayList<>();
        for (int i = 0; i < N_PARTICLES; i++) {
            PVector start = new PVector(random(width), random(height));
            particles.add(new Particle(start, random(10, 15)));
        }
    }

    public void draw() {
        flowfield.update();

        if (debug)
            flowfield.display();

        for (Particle p : particles) {
            p.follow(flowfield);
            p.run();
        }
    }
}
