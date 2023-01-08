/*
 * Core graphical algorithm by Daniel Shiffman @ https://thecodingtrain.com/CodingChallenges/024-perlinnoiseflowfield.html
 */

package com.w2sv.autocrop.flowfield;

import java.util.ArrayList;
import java.util.Set;

import processing.core.PApplet;
import processing.core.PGraphics;

public class Sketch extends PApplet {

    static class Config{
        static final int N_PARTICLES = 400;
        static final int ALPHA_DROP_PERIOD = 350;

        static final int PARTICLE_START_VELOCITY_LOW = 1;
        static final int PARTICLE_START_VELOCITY_HIGH = 3;
        static final int PARTICLE_COLOR_CHANGE_PERIOD = 3000;
        static final float PARTICLE_STROKE_ALPHA = 48;
        static final int PARTICLE_STROKE_WEIGHT = 2;
        static final Set<Integer> PARTICLE_COLORS = Set.of(
                0xFFBC275E,  // magenta bright
                0xFF911945,  // magenta saturated
                0xFF701145,  // magenta dark
                0xFFB00020,  // red
                0xFF6B13B5,  // purple
                0xFF1a0ac7   // ocean blue
        );
        static final int FLOW_FIELD_RESOLUTION = 200;
        static final float FLOW_FIELD_Z_OFF_INCREMENT = 0.004f;
    }

    private final FlowField flowfield = new FlowField();
    private final ArrayList<Particle> particles = new ArrayList<>();
    private final AlphaDropper alphaDropper = new AlphaDropper();

    public Sketch(int width, int height) {
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

        for (int i = 0; i < Config.N_PARTICLES; i++)
            particles.add(new Particle(this));
    }

    @Override
    public void draw() {
        flowfield.update(particles, this);

        alphaDropper.dropAlphaIfDue(millis(), g);
        Particle.colorHandler.changeColorIfDue(millis(), g);

        for (Particle p : particles) {
            p.update();
            p.draw(g);
        }
    }
}

class AlphaDropper{
    private final PeriodicalRunner periodicalRunner = new PeriodicalRunner(Sketch.Config.ALPHA_DROP_PERIOD);

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