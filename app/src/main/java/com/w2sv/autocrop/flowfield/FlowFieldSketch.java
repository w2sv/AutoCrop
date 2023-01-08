/*
 * Core graphical algorithm by Daniel Shiffman @ https://thecodingtrain.com/CodingChallenges/024-perlinnoiseflowfield.html
 */

package com.w2sv.autocrop.flowfield;

import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PGraphics;

public class FlowFieldSketch extends PApplet {

    private final FlowField flowfield = new FlowField();
    private final ArrayList<Particle> particles = new ArrayList<>();
    private final AlphaDropper alphaDropper = new AlphaDropper();

    public FlowFieldSketch(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public void settings() {
        size(width, height, JAVA2D);
    }

    @Override
    public void setup() {
        frameRate(120);
        background(0);

        // initialize particles
        Particle.setFlowFieldDimensions(width, height);
        Particle.initializeCanvas(g);

        for (int i = 0; i < 400; i++)
            particles.add(new Particle());
    }

    @Override
    public void draw() {
        flowfield.update(particles);

        alphaDropper.dropAlphaIfDue(millis(), g);
        Particle.colorHandler.changeColorIfDue(millis(), g);

        for (Particle p : particles) {
            p.update();
            p.draw(g);
        }
    }
}

class AlphaDropper{
    private final PeriodicalRunner periodicalRunner = new PeriodicalRunner(350);

    void dropAlphaIfDue(int millis, PGraphics canvas) {
        periodicalRunner.runIfDue(millis, () -> alphaDrop(canvas));
    }

    private void alphaDrop(PGraphics canvas) {
        canvas.loadPixels();

        attenuateColorIntensities(canvas);

        // Catch 'processing java.lang.IllegalStateException: Can't call setPixels() on a recycled bitmap',
        // occurring upon class being destroyed due to e.g. screen rotation whilst updating pixels
        try {
            canvas.updatePixels();
        } catch (IllegalStateException ignored) {
        }
    }

    private void attenuateColorIntensities(PGraphics canvas) {
        final int UNSET = -16777216;

        for (int i = 0; i < canvas.pixels.length; i++) {
            int argb = canvas.pixels[i];
            if (argb != UNSET) {
                canvas.pixels[i] = UNSET |
                        attenuatedValue((argb >> 16) & 0xFF) << 16 |
                        attenuatedValue((argb >> 8) & 0xFF) << 8 |
                        attenuatedValue(argb & 0xFF);
            }
        }
    }

    private int attenuatedValue(int value) {
        if (value == 0)
            return value;
        return value >= 32 ? value - (value >> 5) : value - 1;
    }
}