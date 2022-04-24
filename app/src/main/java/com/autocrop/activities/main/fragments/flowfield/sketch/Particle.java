package com.autocrop.activities.main.fragments.flowfield.sketch;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

class Particle extends PApplet {
    private static int color;

    private static int[] COLORS;

    public static void initializeColors(PApplet applet){
        final int MAGENTA = applet.color(199, 32, 65);
        final int LIGHT_MAGENTA = applet.color(199, 10, 89);
        final int DARK_RED = applet.color(153, 15, 36);

        COLORS = new int[]{MAGENTA, LIGHT_MAGENTA, DARK_RED};
    }

    /**
     * in seconds
     */
    public static final int COLOR_CHANGE_FREQUENCY = 5;

    static public void setRandomColor(PApplet applet){
        color = COLORS[floor(applet.random(0, COLORS.length))];
    }

    private static final float PARTICLE_START_VELOCITY_LOWER_BOUND = 5;
    private static final float PARTICLE_START_VELOCITY_UPPER_BOUND = 7;

    private static final float PARTICLE_VELOCITY_MAX_LOWER_BOUND = 12;
    private static final float PARTICLE_VELOCITY_MAX_UPPER_BOUND = 18;

    private static final float PARTICLE_STROKE_WEIGHT = 2;

    PVector pos;
    PVector acc;
    private final PVector vel;
    private final PVector previousPos;
    private final float maxSpeed;

    private final int width;
    private final int height;

    public Particle(int width, int height) {
        this.width = width;
        this.height = height;

        maxSpeed = random(PARTICLE_VELOCITY_MAX_LOWER_BOUND, PARTICLE_VELOCITY_MAX_UPPER_BOUND);

        pos = new PVector(random(width), random(height));
        previousPos = pos.copy();

        vel = new PVector(randomVelocity(), randomVelocity());
        acc = new PVector(0, 0);
    }

    private float randomVelocity() {
        return random(PARTICLE_START_VELOCITY_LOWER_BOUND, PARTICLE_START_VELOCITY_UPPER_BOUND);
    }

    public void update() {
        vel.limit(maxSpeed);
        pos.add(vel);
        stayWithinBounds();

        vel.add(acc);
        acc.mult(0);
    }

    private void stayWithinBounds() {
        boolean changedCoordinate = false;

        if (pos.x > width) {
            pos.x = 0;
            changedCoordinate = true;
        } else if (pos.x < 0) {
            pos.x = width;
            changedCoordinate = true;
        }

        if (pos.y > height) {
            pos.y = 0;
            changedCoordinate = true;
        } else if (pos.y < 0) {
            pos.y = height;
            changedCoordinate = true;
        }

        if (changedCoordinate)
            updatePreviousPos();
    }

    public void show(PGraphics canvas, int alpha) {
        canvas.stroke(color, alpha);
        canvas.strokeWeight(PARTICLE_STROKE_WEIGHT);
        canvas.line(pos.x, pos.y, previousPos.x, previousPos.y);
        canvas.point(pos.x, pos.y);  // ?
        updatePreviousPos();
    }

    private void updatePreviousPos() {
        previousPos.x = pos.x;
        previousPos.y = pos.y;
    }
}
