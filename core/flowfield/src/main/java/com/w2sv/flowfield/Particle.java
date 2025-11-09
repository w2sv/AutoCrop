package com.w2sv.flowfield;

import static processing.core.PApplet.cos;
import static processing.core.PApplet.sin;

import processing.core.PGraphics;
import processing.core.PVector;

class Particle {
    private final PVector vel;
    private final PVector acc = new PVector();
    private final PVector previousPos = new PVector();
    private final float maxSpeed;

    PVector pos;
    private boolean skipDraw = false;

    public Particle(PVector vel, float maxSpeed, PVector startPos) {
        this.vel = vel;
        this.maxSpeed = maxSpeed;
        this.pos = startPos;
        this.previousPos.set(startPos);
    }

    /**
     * Updates position, handles edge wrapping, and applies acceleration.
     */
    public void update(float angle, int width, int height) {
        acc.set(cos(angle), sin(angle));
        vel.add(acc).limit(maxSpeed);
        pos.add(vel);
        skipDraw = wrapPositionIfOutOfBounds(width, height);
    }

    /**
     * Wraps the particle around when it leaves the screen bounds.
     * Returns true if position was wrapped (to skip drawing the line).
     */
    private boolean wrapPositionIfOutOfBounds(int width, int height) {
        boolean wrapped = false;

        if (pos.x >= width) {
            pos.x -= width;
            wrapped = true;
        } else if (pos.x < 0) {
            pos.x += width;
            wrapped = true;
        }

        if (pos.y >= height) {
            pos.y -= height;
            wrapped = true;
        } else if (pos.y < 0) {
            pos.y += height;
            wrapped = true;
        }

        return wrapped;
    }

    /**
     * Draws a trail line from the previous to the current position.
     */
    public void draw(PGraphics canvas) {
        if (!skipDraw) {
            canvas.line(pos.x, pos.y, previousPos.x, previousPos.y);
        }
        skipDraw = false;
        previousPos.set(pos);
    }
}
