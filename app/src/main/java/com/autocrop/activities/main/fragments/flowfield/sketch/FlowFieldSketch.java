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
        flowfield.update();

        // initialize particles
        Particle.staticInitialization(width, height);

        for (int i = 0; i < 800; i++)
            particles.add(new Particle());
    }

    private void alphaDrop(){
        final float REDUCTION_COEFF = 0.997f;

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

    int nDrops = 0;
    void dropAlphaIfAppropriate(){
        if (millis() / 200 > nDrops) {
            alphaDrop();
            nDrops += 1;
        }
    }

    public void draw(){
        Particle.colorAdministrator.changeColorIfApplicable(second());

        flowfield.update();

        dropAlphaIfAppropriate();
        for (Particle p : particles) {
            flowfield.affect(p);
            p.update();
            p.draw(g, 47);
        }
    }
}