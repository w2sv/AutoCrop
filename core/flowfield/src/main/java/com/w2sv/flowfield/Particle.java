package com.w2sv.flowfield;

import processing.core.PGraphics;
import processing.core.PVector;

class Particle {
    private final PVector previousPos;
    private final PVector vel;
    private final float maxSpeed;
    PVector pos;
    private PVector acc; // TODO: dont accelerate; just pick velocity and stick with it
    private boolean skipDraw = false;

    public Particle(PVector vel, float maxSpeed, PVector startPos) {
        this.vel = vel;
        this.maxSpeed = maxSpeed;
        this.pos = startPos;
        this.previousPos = pos.copy();
    }

    void applyForceVector(PVector v) {
        acc = v;
    }

    public void update(int xMax, int yMax) {
        pos.add(vel);

        if (invertPosEdgesIfNecessary(xMax, yMax)) {
            skipDraw = true;
        }

        vel.add(acc).limit(maxSpeed);
    }

    /**
     * @return boolean: whether any pos-coordinate has been modified to correspond to opposing display edge
     */
    private boolean invertPosEdgesIfNecessary(int xMax, int yMax) {
        boolean invertedEdge = false;

        // x-edges
        if (pos.x > xMax) {
            pos.x = 0;
            invertedEdge = true;
        } else if (pos.x < 0) {
            pos.x = xMax;
            invertedEdge = true;
        }

        // y-edges
        if (pos.y > yMax) {
            pos.y = 0;
            invertedEdge = true;
        } else if (pos.y < 0) {
            pos.y = yMax;
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
