/*
 * Core graphical algorithm by Daniel Shiffman @ https://thecodingtrain.com/CodingChallenges/024-perlinnoiseflowfield.html
 */

package com.w2sv.autocrop.flowfield;

import java.util.ArrayList;

import processing.core.PApplet;
import timber.log.Timber;

public class FlowFieldSketch extends PApplet {

    private final ArrayList<Particle> particles = new ArrayList<>();
    private FlowField flowfield;
    private int lastAlphaDrop = -1;

    public FlowFieldSketch(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void settings() {
        size(width, height, JAVA2D);
    }

    public void setup() {
        frameRate(120);
        background(0);

        // initialize flowfield
        flowfield = new FlowField();

        // initialize particles
        Particle.setFlowFieldDimensions(width, height);
        Particle.initializeCanvas(g);

        for (int i = 0; i < 400; i++)
            particles.add(new Particle());
    }

    public void draw() {
        dropAlphaIfDue();

        flowfield.update(particles);

        Particle.colorHandler.changeColorIfDue(second(), g);

        for (Particle p : particles) {
            p.update();
            p.draw(g);
        }
    }

    private void dropAlphaIfDue() {
        int t = millis();
        if (t - lastAlphaDrop >= 350) {
            alphaDrop();
            lastAlphaDrop = t;
        }
    }

    private void alphaDrop() {
        g.loadPixels();

        int t1 = millis();

        attenuateColorIntensities();

        Timber.i("Took %sms", millis() - t1);

        // Catch 'processing java.lang.IllegalStateException: Can't call setPixels() on a recycled bitmap',
        // occurring upon class being destroyed due to e.g. screen rotation whilst updating pixels
        try {
            g.updatePixels();
        } catch (IllegalStateException ignored) {
        }
    }

    private void attenuateColorIntensities() {
        final int UNSET = -16777216;

        for (int i = 0; i < g.pixels.length; i++) {
            int argb = g.pixels[i];
            if (argb != UNSET) {
                g.pixels[i] = UNSET |
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