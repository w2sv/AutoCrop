/*
 * Core graphical algorithm by Daniel Shiffman @ https://thecodingtrain.com/CodingChallenges/024-perlinnoiseflowfield.html
 */

package com.w2sv.autocrop.activities.main.fragments.flowfield.sketch;

import java.util.ArrayList;

import processing.core.PApplet;
import timber.log.Timber;

public class FlowFieldSketch extends PApplet {

    private final ArrayList<Particle> particles = new ArrayList<>();
    private FlowField flowfield;
    private int lastAlphaDrop = 0;

    public FlowFieldSketch(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void settings() {
        size(width, height, JAVA2D);
    }

    public void setup() {
        background(0);
        strokeWeight(2);

        // initialize flowfield
        flowfield = new FlowField();

        // initialize particles
        Particle.staticInitialization(width, height);

        for (int i = 0; i < 800; i++)
            particles.add(new Particle());
    }

    public void draw() {
        Particle.colorAdministrator.changeColorIfApplicable(second());
        dropAlphaIfAppropriate();

        flowfield.update(particles);

        for (Particle p : particles) {
            p.update();
            p.draw(g, 23);
        }
    }

    private void dropAlphaIfAppropriate() {
        int second = second();
        if (second != lastAlphaDrop && second % 3 == 0) {
            alphaDrop();
            lastAlphaDrop = second;
        }
    }

    private void alphaDrop() {
        final float REDUCTION_COEFF = 0.85f;

        g.loadPixels();
        for (int i = 0; i < g.pixels.length; i++) {
            int pixel = g.pixels[i];
            g.pixels[i] = color(
                    red(pixel) * REDUCTION_COEFF,
                    green(pixel) * REDUCTION_COEFF,
                    blue(pixel) * REDUCTION_COEFF
            );
        }

        // Catch 'processing java.lang.IllegalStateException: Can't call setPixels() on a recycled bitmap',
        // occurring upon class being destroyed due to e.g. screen rotation whilst updating pixels
        try {
            g.updatePixels();
        } catch (IllegalStateException ignored) {
            Timber.i("Caught exception");
        }
    }
}