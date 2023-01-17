/*
 * Core graphical algorithm by Daniel Shiffman @ https://thecodingtrain.com/CodingChallenges/024-perlinnoiseflowfield.html
 */

package com.w2sv.autocrop.flowfield;

import com.google.common.collect.Sets;

import java.util.ArrayList;
import java.util.Set;

import processing.core.PApplet;
import processing.core.PGraphics;

public class Sketch extends PApplet {

    private final FlowField flowfield = new FlowField();
    private final ArrayList<Particle> particles = new ArrayList<>();
    private final AlphaDropper alphaDropper = new AlphaDropper();
    private final ColorHandler colorHandler = new ColorHandler();

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
        // enable 120fps for devices being capable of it; Otherwise the fps produced by the sketch will
        // accommodate the hardware-determined limit
        frameRate(120);
        background(Config.BACKGROUND_COLOR);

        colorHandler.setStrokeColor(g);
        g.strokeWeight(Sketch.Config.PARTICLE_STROKE_WEIGHT);

        // initialize particles
        Particle.setFlowFieldDimensions(width, height);

        for (int i = 0; i < Config.N_PARTICLES; i++)
            particles.add(new Particle(this));
    }

    @Override
    public void draw() {
        flowfield.updateAndApplyTo(particles.iterator(), this);

        alphaDropper.dropAlphaIfDue(millis(), g);
        colorHandler.changeColorIfDue(millis(), g);

        for (Particle p : particles) {
            p.update();
            p.draw(g);
        }
    }

    static class Config {
        static final int N_PARTICLES = 600;
        static final int ALPHA_DROP_PERIOD = 350;
        static final int PARTICLE_START_VELOCITY_LOW = 1;
        static final int PARTICLE_START_VELOCITY_HIGH = 3;
        static final int PARTICLE_MAX_VELOCITY_LOW = 6;
        static final int PARTICLE_MAX_VELOCITY_HIGH = 8;
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
        static final int BACKGROUND_COLOR = 0;
        static final int FLOW_FIELD_GRANULARITY = 200;
        static final float FLOW_FIELD_Z_OFF_INCREMENT = 0.004f;
    }
}

/**
 * Handles period color changing & inherent random picking, color-dependent canvas modification
 */
class ColorHandler {

    private final PeriodicalRunner runner = new PeriodicalRunner(Sketch.Config.PARTICLE_COLOR_CHANGE_PERIOD);
    public int color = Random.randomElement(new ArrayList<>(Sketch.Config.PARTICLE_COLORS));

    public void changeColorIfDue(int millis, PGraphics canvas) {
        runner.runIfDue(millis, () -> {
            setNewRandomlyPickedColor();
            setStrokeColor(canvas);
        });
    }

    public void setStrokeColor(PGraphics canvas) {
        canvas.stroke(color, Sketch.Config.PARTICLE_STROKE_ALPHA);
    }

    private void setNewRandomlyPickedColor() {
        color = Random.randomElement(new ArrayList<>(Sets.difference(Sketch.Config.PARTICLE_COLORS, Set.of(color))));
    }
}

class AlphaDropper {
    private final PeriodicalRunner periodicalRunner = new PeriodicalRunner(Sketch.Config.ALPHA_DROP_PERIOD);

    void dropAlphaIfDue(int millis, PGraphics canvas) {
        periodicalRunner.runIfDue(millis, () -> dropAlpha(canvas));
    }

    private void dropAlpha(PGraphics canvas) {
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
        return value >= 32 ? value - (value >> 5) : PApplet.max(value - 2, 0);
    }
}