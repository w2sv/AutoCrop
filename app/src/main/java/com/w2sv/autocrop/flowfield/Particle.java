package com.w2sv.autocrop.flowfield;

import androidx.annotation.NonNull;

import com.google.common.collect.Sets;

import java.util.ArrayList;
import java.util.Set;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

class Particle {
    static ColorHandler colorHandler;
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
        colorHandler = new ColorHandler();
        colorHandler.setStrokeColor(canvas);
        canvas.strokeWeight(Sketch.Config.PARTICLE_STROKE_WEIGHT);
    }

    public Particle(PApplet parent) {
        vel = new PVector(
                parent.random(Sketch.Config.PARTICLE_START_VELOCITY_LOW, Sketch.Config.PARTICLE_START_VELOCITY_HIGH),
                parent.random(Sketch.Config.PARTICLE_START_VELOCITY_LOW, Sketch.Config.PARTICLE_START_VELOCITY_HIGH)
        );
        acc = new PVector(0, 0);
        maxSpeed = parent.random(6, 8);

        pos = new PVector(parent.random(flowFieldWidth), parent.random(flowFieldHeight));
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

        public int color = Random.randomElement(new ArrayList<>(Sketch.Config.PARTICLE_COLORS));
        private final PeriodicalRunner runner = new PeriodicalRunner(Sketch.Config.PARTICLE_COLOR_CHANGE_PERIOD);

        public void changeColorIfDue(int millis, PGraphics canvas) {
            runner.runIfDue(millis, () -> {
                setNewRandomlyPickedColor();
                setStrokeColor(canvas);
            });
        }

        public void setStrokeColor(PGraphics canvas) {
            canvas.stroke(color, Sketch.Config.PARTICLE_STROKE_ALPHA);
        }

        private void setNewRandomlyPickedColor() {
            color = Random.randomElement(new ArrayList<>(Sets.difference(Sketch.Config.PARTICLE_COLORS, Set.of(color))));
        }
    }
}
