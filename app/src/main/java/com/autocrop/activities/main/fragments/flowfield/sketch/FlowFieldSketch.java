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
        flowfield = new FlowField(width, height);

        // initialize particles
        Particle.staticInitialization(width, height);

        for (int i = 0; i < 800; i++)
            particles.add(new Particle());
    }

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

    int lastDrop = 0;
    void dropAlphaIfAppropriate(){
        int second = second();
        if (second != lastDrop && second % 3 == 0) {
            alphaDrop();
            lastDrop = second;
        }
    }

    public void draw(){
        Particle.colorAdministrator.changeColorIfApplicable(second());
        dropAlphaIfAppropriate();

        flowfield.update();

        for (Particle p : particles) {
            flowfield.affect(p);
            p.update();
            p.draw(g, 23);
        }
    }
}