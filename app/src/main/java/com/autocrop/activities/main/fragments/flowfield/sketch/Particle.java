package com.autocrop.activities.main.fragments.flowfield.sketch;

import androidx.annotation.NonNull;

import com.autocrop.utils.kotlin.Random;
import com.google.common.collect.Sets;

import java.util.ArrayList;
import java.util.Set;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

class Particle extends PApplet {
    static ColorAdministrator colorAdministrator;
    private static int width;
    private static int height;
    private final PVector previousPos;
    private final PVector acc;
    private final PVector vel;
    private final float maxSpeed;
    PVector pos;
    private boolean skipDraw = false;
    public Particle() {
        vel = new PVector(randomStartVelocity(), randomStartVelocity());
        acc = new PVector(0, 0);
        maxSpeed = random(6, 8);

        pos = new PVector(random(width), random(height));
        previousPos = pos.copy();
    }

    private static void initializeColorAdministrator() {
        colorAdministrator = new ColorAdministrator();
    }

    public static void staticInitialization(int width, int height) {
        Particle.width = width;
        Particle.height = height;

        initializeColorAdministrator();
    }

    private float randomStartVelocity() {
        return random(1, 3);
    }

    void applyFlowFieldVector(PVector v) {
        acc.add(v);
    }

    public void update() {
        vel.limit(maxSpeed);
        pos.add(vel);

        if (invertEdgeIfNecessary(pos)) {
            updatePreviousPos();
            skipDraw = true;
        }

        vel.add(acc);
        acc.set(0, 0);
    }

    private boolean invertEdgeIfNecessary(PVector pos) {
        boolean invertedEdge = false;

        if (pos.x > width) {
            pos.x = 0;
            invertedEdge = true;
        } else if (pos.x < 0) {
            pos.x = width;
            invertedEdge = true;
        }

        if (pos.y > height) {
            pos.y = 0;
            invertedEdge = true;
        } else if (pos.y < 0) {
            pos.y = height;
            invertedEdge = true;
        }

        return invertedEdge;
    }

    public void draw(@NonNull PGraphics canvas, int alpha) {
        if (skipDraw)
            skipDraw = false;
        else {
            canvas.stroke(colorAdministrator.color, alpha);
            canvas.line(pos.x, pos.y, previousPos.x, previousPos.y);
            updatePreviousPos();
        }
    }

    private void updatePreviousPos() {
        previousPos.x = pos.x;
        previousPos.y = pos.y;
    }

    static class ColorAdministrator {
        private final Set<Integer> COLORS = Set.of(
                -3727295,  // magenta
                -3732903,  // light magenta
                -6746332,  // dark red
                -15070521  // blue
        );
        public int color;
        private int lastColorChange = 0;

        ColorAdministrator() {
            color = Random.randomElement(new ArrayList<>(COLORS));
        }

        public void changeColorIfApplicable(int second) {
            if (second != lastColorChange && second % 5 == 0) {
                pickNewColor();
                lastColorChange = second;
            }
        }

        private void pickNewColor() {
            Set<Integer> unusedColors = Sets.difference(COLORS, Set.of(color));
            color = Random.randomElement(new ArrayList<>(unusedColors));
        }
    }
}
