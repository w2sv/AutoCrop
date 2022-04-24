/*
 * Core graphical algorithm by Daniel Shiffman @ https://thecodingtrain.com/CodingChallenges/024-perlinnoiseflowfield.html
 */

package com.autocrop.activities.main.fragments.flowfield.sketch;

import static processing.core.PApplet.max;

import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PGraphics;

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
        Particle.setRandomColor(this);
    }

    private final PixelFader pixelFader = new PixelFader(2);

    public void draw(){
        changeColorIfApplicable();

        pixelFader.fadePixelsIfApplicable(frameCount, g);
        flowfield.update();

        for (Particle p : particles) {
            flowfield.affect(p);
            p.update();
            p.show(g, 30);
        }

//        alpha.decreaseIfApplicable(millis());
    }

    private int lastParticleColorChangeSecond;

    private void changeColorIfApplicable(){
        int second = second();

        if (second != lastParticleColorChangeSecond && second % Particle.COLOR_CHANGE_FREQUENCY == 0){
            Particle.setRandomColor(this);
            lastParticleColorChangeSecond = second;
        }
    }
}

class GraduallyDecreasingValue {

    public int value;
    private final int minValue;
    private final int millisecondsPerDecrease;
    private int decreaseCheckPoint;

    GraduallyDecreasingValue(int initialValue, int minValue, int millisecondsPerDecrease){
        value = initialValue;
        this.minValue = minValue;
        this.millisecondsPerDecrease = millisecondsPerDecrease;
        decreaseCheckPoint = millisecondsPerDecrease;
    }

    public void decreaseIfApplicable(int millis){
        if (value > minValue && millis > decreaseCheckPoint){
            value -= 1;
            decreaseCheckPoint += millisecondsPerDecrease;
        }
    }
}

class PixelFader{

    private final int framesPerStep;

    PixelFader(int framesPerStep){
        this.framesPerStep = framesPerStep;
    }

    public void fadePixelsIfApplicable(int frameCount, PGraphics canvas){
        if (frameCount % framesPerStep == 0)
            fadePixels(canvas);
    }

    /**
     * By benja @ https://forum.processing.org/two/discussion/13189/a-better-way-to-fade
     */
    private static void fadePixels(PGraphics canvas) {
        canvas.loadPixels();

        // iterate over pixels
        for (int i = 0; i < canvas.pixels.length; i++) {

            // get alpha value
            int alpha = (canvas.pixels[i] >> 24) & 0xFF;

            // reduce alpha value
            alpha = max(0, alpha - 3);

            // assign color with new alpha-value
            canvas.pixels[i] = alpha << 24 | (canvas.pixels[i]) & 0xFFFFFF;
        }
        canvas.updatePixels();
    }
}