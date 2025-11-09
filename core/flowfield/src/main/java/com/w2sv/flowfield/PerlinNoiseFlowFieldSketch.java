/**
 * Core algorithm by Daniel Shiffman @ https://thecodingtrain.com/CodingChallenges/024-perlinnoiseflowfield.html
 */

package com.w2sv.flowfield;

import com.w2sv.flowfield.helper.ColorHandler;
import com.w2sv.flowfield.helper.ColorIntensityReducer;
import com.w2sv.flowfield.helper.FpsLogger;

import java.util.Set;

import processing.core.PApplet;
import processing.core.PVector;

public class PerlinNoiseFlowFieldSketch extends PApplet {

    private final FlowField flowfield = new FlowField(Config.FLOW_FIELD_GRANULARITY, Config.FLOW_FIELD_Z_OFF_INCREMENT);
    private final Particle[] particles = new Particle[Config.N_PARTICLES];
    private final ColorIntensityReducer colorIntensityReducer = new ColorIntensityReducer(Config.ALPHA_DROP_PERIOD, () -> g);
    private final ColorHandler colorHandler = new ColorHandler(Config.PARTICLE_COLOR_CHANGE_PERIOD, Config.PARTICLE_COLORS, Config.PARTICLE_STROKE_ALPHA, () -> g);
    private final FpsLogger fpsLogger = new FpsLogger(1_000, System.out::println);

    public PerlinNoiseFlowFieldSketch(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public void settings() {
        // Use P3D for hardware acceleration and higher performance,
        // even though only 2D rendering is used. P3D runs on OpenGL,
        // while JAVA2D is CPU-bound and much slower for large particle counts.
        size(width, height, P3D);
    }

    @Override
    public void setup() {
        // enable 120fps for devices being capable of it; Otherwise the fps produced by the sketch will
        // accommodate the hardware-determined limit
        frameRate(120);
        background(Config.BACKGROUND_COLOR);

        colorHandler.setStrokeColor(g);
        g.strokeWeight(Config.PARTICLE_STROKE_WEIGHT);

        initializeParticles();
    }

    private void initializeParticles() {
        for (int i = 0; i < Config.N_PARTICLES; i++) {
            particles[i] = new Particle(
                new PVector(
                    random(Config.PARTICLE_START_VELOCITY_LOW, Config.PARTICLE_START_VELOCITY_HIGH),
                    random(Config.PARTICLE_START_VELOCITY_LOW, Config.PARTICLE_START_VELOCITY_HIGH)
                ),
                random(Config.PARTICLE_MAX_VELOCITY_LOW, Config.PARTICLE_MAX_VELOCITY_HIGH),
                new PVector(random(width), random(height))
            );
        }
    }

    @Override
    public void draw() {
        fpsLogger.run(frameRate, millis());

        flowfield.prepareFrame();

        for (Particle p : particles) {
            float forceAngle = flowfield.forceAngle(p.pos, this);
            p.update(forceAngle, width, height);
            p.draw(g);
        }

        colorIntensityReducer.reduceColorIntensitiesIfPeriodElapsed(millis());
        colorHandler.changeColorIfPeriodElapsed(millis());
    }

    static class Config {
        static final int N_PARTICLES = 600;
        static final int ALPHA_DROP_PERIOD = 350;
        static final int PARTICLE_START_VELOCITY_LOW = 1;
        static final int PARTICLE_START_VELOCITY_HIGH = 3;
        static final int PARTICLE_MAX_VELOCITY_LOW = 6;
        static final int PARTICLE_MAX_VELOCITY_HIGH = 8;
        static final int PARTICLE_COLOR_CHANGE_PERIOD = 3000;
        static final float PARTICLE_STROKE_ALPHA = 102;
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
