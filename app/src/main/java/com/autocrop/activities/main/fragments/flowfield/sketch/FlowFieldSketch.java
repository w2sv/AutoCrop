/*
 * Core graphical algorithm by Daniel Shiffman @ https://thecodingtrain.com/CodingChallenges/024-perlinnoiseflowfield.html
 */

package com.autocrop.activities.main.fragments.flowfield.sketch;

import java.util.ArrayList;

import processing.core.PApplet;

public class FlowFieldSketch extends PApplet {

    public FlowFieldSketch(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void settings() {
        size(width, height, JAVA2D);
    }

    private FlowField flowfield;
    private final ArrayList<Particle> particles = new ArrayList<>();

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

    public void draw(){
        Particle.colorAdministrator.changeColorIfApplicable(second());
        dropAlphaIfAppropriate();

        flowfield.update(particles);

        for (Particle p : particles) {
            p.update();
            p.draw(g, 23);
        }
    }

    private void dropAlphaIfAppropriate(){
        int second = second();
        if (second != lastAlphaDrop && second % 3 == 0) {
            alphaDrop();
            lastAlphaDrop = second;
        }
    }
    private int lastAlphaDrop = 0;

    private void alphaDrop(){
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
        g.updatePixels();
    }
}