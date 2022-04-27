package com.autocrop.activities.main.fragments.flowfield.sketch;

import com.autocrop.utils.Random;

import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

class Particle extends PApplet {
    private static int[] COLORS;

    public static void initializeColors(PApplet applet){
        final int MAGENTA = applet.color(199, 32, 65);
        final int LIGHT_MAGENTA = applet.color(199, 10, 89);
        final int DARK_RED = applet.color(153, 15, 36);
        final int BLUE = applet.color(26, 10, 199);

        COLORS = new int[]{MAGENTA, LIGHT_MAGENTA, DARK_RED, BLUE};
    }

    private static int colorIndex;

    static public void setRandomNewColor(){
        ArrayList<Integer> unusedColorIndices = new ArrayList<>();
        for (int i = 0; i < COLORS.length; i++){
            if (i != colorIndex)
                unusedColorIndices.add(i);
        }

        colorIndex = Random.randomElement(unusedColorIndices);
    }

    public static final int COLOR_CHANGE_FREQUENCY = 5;

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

        vel = new PVector(startVelocity(), startVelocity());
        acc = new PVector(0, 0);
        maxSpeed = random(6, 8);

        pos = new PVector(random(width), random(height));
        previousPos = pos.copy();
    }

    private float startVelocity() {
        return random(1, 3);
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
        canvas.stroke(COLORS[colorIndex], alpha);
        canvas.strokeWeight(2);
        canvas.line(pos.x, pos.y, previousPos.x, previousPos.y);
        canvas.point(pos.x, pos.y);  // ?
        updatePreviousPos();
    }

    private void updatePreviousPos() {
        previousPos.x = pos.x;
        previousPos.y = pos.y;
    }
}
