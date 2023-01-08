package com.w2sv.autocrop.flowfield;

import androidx.annotation.NonNull;

import com.google.common.collect.Sets;

import java.util.ArrayList;
import java.util.Set;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

class Particle extends PApplet {
    static ColorHandler colorHandler = new ColorHandler();
    private static int flowFieldWidth;
    private static int flowFieldHeight;
    private final PVector previousPos;
    private final PVector acc;
    private final PVector vel;
    private final float maxSpeed;
    PVector pos;
    private boolean skipDraw = false;

    public static void setFlowFieldDimensions(int width, int height) {
        Particle.flowFieldWidth = width;
        Particle.flowFieldHeight = height;
    }

    public static void initializeCanvas(PGraphics canvas) {
        canvas.strokeWeight(2);
    }

    public Particle() {
        vel = new PVector(
                random(Sketch.Config.PARTICLE_START_VELOCITY_LOW, Sketch.Config.PARTICLE_START_VELOCITY_HIGH),
                random(Sketch.Config.PARTICLE_START_VELOCITY_LOW, Sketch.Config.PARTICLE_START_VELOCITY_HIGH)
        );
        acc = new PVector(0, 0);
        maxSpeed = random(6, 8);

        pos = new PVector(random(flowFieldWidth), random(flowFieldHeight));
        previousPos = pos.copy();
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

        if (pos.x > flowFieldWidth) {
            pos.x = 0;
            invertedEdge = true;
        } else if (pos.x < 0) {
            pos.x = flowFieldWidth;
            invertedEdge = true;
        }

        if (pos.y > flowFieldHeight) {
            pos.y = 0;
            invertedEdge = true;
        } else if (pos.y < 0) {
            pos.y = flowFieldHeight;
            invertedEdge = true;
        }

        return invertedEdge;
    }

    public void draw(@NonNull PGraphics canvas) {
        if (skipDraw)
            skipDraw = false;
        else {
            canvas.line(pos.x, pos.y, previousPos.x, previousPos.y);
            updatePreviousPos();
        }
    }

    private void updatePreviousPos() {
        previousPos.x = pos.x;
        previousPos.y = pos.y;
    }

    static class ColorHandler {

        public int color;
        private final PeriodicalRunner runner = new PeriodicalRunner(Sketch.Config.PARTICLE_COLOR_CHANGE_PERIOD);

        ColorHandler() {
            setNewRandomlyPickedColor();
        }

        public void changeColorIfDue(int millis, PGraphics canvas) {
            runner.runIfDue(millis, () -> {
                setNewRandomlyPickedColor();
                canvas.stroke(color, Sketch.Config.PARTICLE_STROKE_ALPHA);
            });
        }

        private void setNewRandomlyPickedColor() {
            color = Random.randomElement(new ArrayList<>(Sets.difference(Sketch.Config.PARTICLE_COLORS, Set.of(color))));
        }
    }
}
