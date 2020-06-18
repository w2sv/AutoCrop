package com.example.screenshotboundremoval;

import processing.core.PApplet;

public class EllipseSketch extends PApplet {
    float[] coordinateArray = {};
    static float xOffset = 0;
    static float yOffset = 0;
    float MULTIPLIER = 100;

    public void settings() {
        size(720, 1400);
    }

    public void setup() {
        background(145, 30, 45);

        for (int i=0; i<400; i++){
            float x = random(0, width);
            float y = random(0, height);
            coordinateArray = append(coordinateArray, x);
            coordinateArray = append(coordinateArray, y);
        }
    }

    public void draw() {
        background(145, 30, 45);

        stroke(0);
        for (int i = 0; i < 300; i++){
            float x = noise(width, height);
            float y = noise(width, height);
            print(x);
            print(y);
            line(x, y, x + xOffset * MULTIPLIER, y + yOffset * MULTIPLIER);
        }
    }
}