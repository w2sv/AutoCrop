package com.example.screenshotboundremoval;

import processing.core.PApplet;
import processing.core.PVector;
import java.util.ArrayList;

public class FlowfieldDriver extends PApplet{
    FlowField flowfield;
    ArrayList<Particle> particles;
    int width = 720;
    int height = 1400;
    int COEFF = 500;

    boolean debug = false;

    public void settings() {
        size(720, 1400, P2D);
    }

    public void setup() {
        flowfield = new FlowField(2);
        flowfield.update();

        particles = new ArrayList<Particle>();
        for (int i = 0; i < 20; i++) {
            PVector start = new PVector(random(width), random(height));
            particles.add(new Particle(start, random(2, 8)));
        }
        background(127);
    }

    public void draw() {
        flowfield.update();

        if (debug)
            flowfield.display();

        for (Particle p : particles) {
            p.follow(flowfield);
            p.run();
            stroke(139, 0, 0, 255); // first -> color, second -> alpha; 139
            strokeWeight(10);
            // line(-width, -height, width, height);

            line(p.pos.x * COEFF, p.pos.y * COEFF, p.previousPos.x * COEFF, p.previousPos.y * COEFF);
            // point(p.pos.x, p.pos.y);
            p.updatePreviousPos();
        }
    }

    // ---------------------FLOW FIELD-------------------------------
    public class FlowField{
        PVector[] vectors;
        int cols, rows;
        float inc = (float) 0.1;
        float zoff = 0;
        int scl;

        FlowField(int res) {
            scl = res;
            cols = floor(width / res) + 1;
            rows = floor(height / res) + 1;
            print(cols);
            print(rows);
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
                    strokeWeight((float)0.1);
                    pushMatrix();
                    translate(x * scl, y * scl);
                    rotate(v.heading());
                    line(0, 0, scl, 0);
                    popMatrix();
                }
            }
        }
    }

    // ----------------------PARTICLE--------------------------
    public class Particle extends PApplet{
        PVector pos;
        PVector vel;
        PVector acc;
        PVector previousPos;
        float maxSpeed;

        Particle(PVector start, float maxspeed) {
            pos = start;
            maxSpeed = maxspeed;
            vel = new PVector(0, 0);
            acc = new PVector(0, 0);
            previousPos = pos.copy();
        }
        void run() {
            update();
            edges();
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
        /*void show() {
            stroke(139, 0, 0, 5); // first -> color, second -> alpha; 139
            strokeWeight(1);
            line(pos.x, pos.y, previousPos.x, previousPos.y);
            //point(pos.x, pos.y);
            updatePreviousPos();
        }*/
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
            this.previousPos.x = pos.x;
            this.previousPos.y = pos.y;
        }
        void follow(FlowField flowfield) {
            int x = floor(pos.x / flowfield.scl);
            int y = floor(pos.y / flowfield.scl);
            print("follow");
            print(x);
            print(y);
            int index = x + y * flowfield.cols;

            if (index >= 0){
                PVector force = flowfield.vectors[index];
                applyForce(force);
            }
        }
    }
}
