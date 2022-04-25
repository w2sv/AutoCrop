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

        int N_PARTICLES = 800;
        for (int i = 0; i < N_PARTICLES; i++)
            particles.add(new Particle(width, height));

        Particle.initializeColors(this);
        Particle.setRandomNewColor();
    }

    public void draw(){
        changeColorIfApplicable();

        flowfield.update();

        for (Particle p : particles) {
            flowfield.affect(p);
            p.update();
            p.show(g, 35);
        }
    }

    private int lastColorChangeSecond;

    private void changeColorIfApplicable(){
        int second = second();

        if (second != lastColorChangeSecond && second % Particle.COLOR_CHANGE_FREQUENCY == 0){
            Particle.setRandomNewColor();
            lastColorChangeSecond = second;
        }
    }
}