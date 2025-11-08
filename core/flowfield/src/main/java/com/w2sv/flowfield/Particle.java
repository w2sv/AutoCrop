package com.w2sv.flowfield;

import processing.core.PGraphics;
import processing.core.PVector;

class Particle {
    private static int flowFieldWidth;
    private static int flowFieldHeight;
    private final PVector previousPos;
    private final PVector vel;
    private final float maxSpeed;
    PVector pos;
    private PVector acc;
    private boolean skipDraw = false;

    public Particle(PVector vel, float maxSpeed, PVector startPos) {
        this.vel = vel;
        this.maxSpeed = maxSpeed;
        this.pos = startPos;
        this.previousPos = pos.copy();
    }

    public static void setFlowFieldDimensions(int width, int height) {
        Particle.flowFieldWidth = width;
        Particle.flowFieldHeight = height;
    }

    void applyForceVector(PVector v) {
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
}
