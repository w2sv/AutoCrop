package com.autocrop.activities.main.fragments.flowfield.sketch;

import com.autocrop.utils.kotlin.Random;
import com.google.common.collect.Sets;

import java.util.ArrayList;
import java.util.Set;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

class Particle extends PApplet {
    private static final Set<Integer> COLORS = Set.of(
            -3727295,  // magenta
            -3732903,  // light magenta
            -6746332,  // dark red
            -15070521  // blue
    );

    static public void changeColorIfApplicable(int second){
        if (second != lastColorChangeSecond && second % COLOR_CHANGE_FREQUENCY == 0){
            pickNewColor();
            lastColorChangeSecond = second;
        }
    }

    private static int lastColorChangeSecond;

    private static int color = -1;

    static public void pickNewColor(){
        Set<Integer> unusedColors = Sets.difference(COLORS, Set.of(color));
        color = Random.randomElement(new ArrayList<>(unusedColors));
    }

    private static final int COLOR_CHANGE_FREQUENCY = 5;

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

        vel = new PVector(randomStartVelocity(), randomStartVelocity());
        acc = new PVector(0, 0);
        maxSpeed = random(6, 8);

        pos = new PVector(random(width), random(height));
        previousPos = pos.copy();
    }

    private float randomStartVelocity() {
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
        canvas.stroke(color, alpha);
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
