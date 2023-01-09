package com.w2sv.autocrop.flowfield;

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
    private PVector acc;
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
        maxSpeed = parent.random(Sketch.Config.PARTICLE_MAX_VELOCITY_LOW, Sketch.Config.PARTICLE_MAX_VELOCITY_HIGH);

        pos = new PVector(parent.random(flowFieldWidth), parent.random(flowFieldHeight));
        previousPos = pos.copy();
    }

    void applyFlowFieldVector(PVector v) {
        acc = v;
    }

    public void update() {
        pos.add(vel);

        if (invertPosEdgesIfNecessary()) {
            skipDraw = true;
        }

        vel.add(acc).limit(maxSpeed);
    }

    /**
     * @return boolean: whether any pos-coordinate has been modified to correspond to opposing display edge
     */
    private boolean invertPosEdgesIfNecessary() {
        boolean invertedEdge = false;

        // x-edges
        if (pos.x > flowFieldWidth) {
            pos.x = 0;
            invertedEdge = true;
        } else if (pos.x < 0) {
            pos.x = flowFieldWidth;
            invertedEdge = true;
        }

        // y-edges
        if (pos.y > flowFieldHeight) {
            pos.y = 0;
            invertedEdge = true;
        } else if (pos.y < 0) {
            pos.y = flowFieldHeight;
            invertedEdge = true;
        }

        return invertedEdge;
    }

    public void draw(PGraphics canvas) {
        if (skipDraw)
            skipDraw = false;
        else
            canvas.line(pos.x, pos.y, previousPos.x, previousPos.y);

        updatePreviousPos();
    }

    private void updatePreviousPos() {
        previousPos.x = pos.x;
        previousPos.y = pos.y;
    }

    /**
     * Handles period color changing & inherent random picking, color-dependent canvas modification
     */
    static class ColorHandler {

        public int color = Random.randomElement(new ArrayList<>(Sketch.Config.PARTICLE_COLORS));
        private final PeriodicalRunner runner = new PeriodicalRunner(Sketch.Config.PARTICLE_COLOR_CHANGE_PERIOD);

        public void changeColorIfDue(int millis, PGraphics canvas) {
            runner.runIfDue(millis, () -> {
                setNewRandomlyPickedColor();
                setStrokeColor(canvas);
            });
        }

        public void setStrokeColor(PGraphics canvas){
            canvas.stroke(color, Sketch.Config.PARTICLE_STROKE_ALPHA);
        }

        private void setNewRandomlyPickedColor() {
            color = Random.randomElement(new ArrayList<>(Sets.difference(Sketch.Config.PARTICLE_COLORS, Set.of(color))));
        }
    }
}
