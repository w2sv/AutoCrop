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
    private ArrayList<Particle> particles;

    public void setup() {
        background(0);

        // initialize flowfield
        flowfield = new FlowField(width, height);
        flowfield.update();

        // initialize particles
        particles = new ArrayList<>();

        for (int i = 0; i < 800; i++)
            particles.add(new Particle(width, height));

        Particle.initializeColorAdministrator();
    }

    public void draw(){
        Particle.colorAdministrator.changeColorIfApplicable(second());

        flowfield.update();

        for (Particle p : particles) {
            flowfield.affect(p);
            p.update();
            p.draw(g, 35);
        }
    }
}