/*
 * Core graphical algorithm by Daniel Shiffman @ https://thecodingtrain.com/CodingChallenges/024-perlinnoiseflowfield.html
 */

package com.autocrop.activities.main.fragments.flowfield;

import android.graphics.Bitmap;
import android.graphics.Point;

import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;
import timber.log.Timber;

public class FlowFieldPApplet extends PApplet {
    final int FLOW_FIELD_RESOLUTION = 200;
    final int N_PARTICLES = 800;

    final float PARTICLE_STROKE_WEIGHT = 2;

    final int MAGENTA = color(199, 32, 65);
    final int LIGHT_MAGENTA = color(199, 10, 89);
    final int DARK_RED = color(153, 15, 36);

    final int[] COLORS = {MAGENTA, LIGHT_MAGENTA, DARK_RED};

    int particleColor;
    int alpha = 55;
    final int MIN_ALPHA = 35;

    final float PARTICLE_START_VELOCITY_LOWER_BOUND = 5;
    final float PARTICLE_START_VELOCITY_UPPER_BOUND = 7;

    final float PARTICLE_VELOCITY_MAX_LOWER_BOUND = 12;
    final float PARTICLE_VELOCITY_MAX_UPPER_BOUND = 18;

    final int FRAMES_PER_FADING_STEP = 3;

    PGraphics canvas;

    public Bitmap bitmap(){
        canvas.loadPixels();
        return Bitmap.createBitmap(canvas.pixels, canvas.width, canvas.height, Bitmap.Config.ARGB_8888);
    }

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
            maxSpeed = random(PARTICLE_VELOCITY_MAX_LOWER_BOUND, PARTICLE_VELOCITY_MAX_UPPER_BOUND);

            pos = new PVector(random(width), random(height));
            previousPos = pos.copy();

            vel = new PVector(randomStartVelocity(), randomStartVelocity());
            acc = new PVector(0, 0);
        }

        private float randomStartVelocity(){
            return random(PARTICLE_START_VELOCITY_LOWER_BOUND, PARTICLE_START_VELOCITY_UPPER_BOUND);
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
            canvas.stroke(particleColor, alpha);
            canvas.strokeWeight(PARTICLE_STROKE_WEIGHT);
            canvas.line(pos.x, pos.y, previousPos.x, previousPos.y);
            canvas.point(pos.x, pos.y);  // ?
            updatePreviousPos();
        }
    }

    FlowField flowfield;
    ArrayList<Particle> particles;

    public FlowFieldPApplet(Point screen_resolution) {
        width = screen_resolution.x;
        height = screen_resolution.y;

        canvas = createGraphics(width, height);
    }

    public void settings() {
        size(width, height, P2D);
    }

    private void setBackground(){
        background(0);
    }

    public void setup() {
        setBackground();
        setParticleColorRandomly();

        flowfield = new FlowField(FLOW_FIELD_RESOLUTION);
        flowfield.update();

        particles = new ArrayList<>();
        for (int i = 0; i < N_PARTICLES; i++) {
            particles.add(new Particle());
        }
    }

    public void draw(){
        setBackground();
        changeColorIfApplicable();

        canvas.beginDraw();

        if (frameCount % FRAMES_PER_FADING_STEP == 0)
            fadePixels();

        flowfield.update();

        for (Particle p : particles) {
            flowfield.affect(p);
            p.update();
            p.show();
        }

        canvas.endDraw();
        image(canvas, 0, 0);

        graduallyDecreaseAlphaIfApplicable();
    }

    final int MILLISECONDS_PER_ALPHA_DECREASE = 400;
    int decreaseCheckPoint = MILLISECONDS_PER_ALPHA_DECREASE;

    private void graduallyDecreaseAlphaIfApplicable(){
        if (alpha > MIN_ALPHA && millis() > decreaseCheckPoint){
            alpha -= 1;
            decreaseCheckPoint += MILLISECONDS_PER_ALPHA_DECREASE;
            Timber.i("Decreased alpha");
        }
    }

    final int SECONDS_BETWEEN_COLOR_CHANGE = 5;
    int blockedSecond = -1;

    private void setParticleColorRandomly(){
        particleColor = COLORS[floor(random(0, COLORS.length))];
    }

    private void changeColorIfApplicable(){
        int second = second();
        if (second != blockedSecond && second % SECONDS_BETWEEN_COLOR_CHANGE == 0){
            blockedSecond = second;
            setParticleColorRandomly();
        }
    }

    /**
     * By benja @ https://forum.processing.org/two/discussion/13189/a-better-way-to-fade
     */
    private void fadePixels() {
        canvas.loadPixels();

        // iterate over pixels
        for (int i = 0; i < canvas.pixels.length; i++) {

            // get alpha value
            int alpha = (canvas.pixels[i] >> 24) & 0xFF;

            // reduce alpha value
            alpha = max(0, alpha - 1);

            // assign color with new alpha-value
            canvas.pixels[i] = alpha << 24 | (canvas.pixels[i]) & 0xFFFFFF;
        }

        canvas.updatePixels();
    }
}
