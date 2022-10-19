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

        // initialize flowfield
        flowfield = new FlowField(width, height);
        flowfield.update();

        // initialize particles
        Particle.staticInitialization(width, height);

        for (int i = 0; i < 800; i++)
            particles.add(new Particle());
    }

    private void alphaDrop(){
        final int REDUCE_BY = 4;

        g.loadPixels();
        for (int i = 0; i < g.pixels.length; i++) {
            int pixel = g.pixels[i];
            g.pixels[i] = color(
                max(red(pixel) - REDUCE_BY, 0),
                max(green(pixel) - REDUCE_BY, 0),
                max(blue(pixel) - REDUCE_BY, 0),
                255
            );
        }
        g.updatePixels();
    }

    int lastAlphaDrop = 0;
    void dropAlphaIfAppropriate(){
        int second = second();
        if (second != lastAlphaDrop && second % 2 == 0) {
            alphaDrop();
            lastAlphaDrop = second;
        }
    }

    public void draw(){
        Particle.colorAdministrator.changeColorIfApplicable(second());

        flowfield.update();

        dropAlphaIfAppropriate();
        for (Particle p : particles) {
            flowfield.affect(p);
            p.update();
            p.draw(g, 35);
        }
    }
}