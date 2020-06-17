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

        for (int i=0; i<100; i++){
            float x = random(0, width);
            float y = random(0, height);
            coordinateArray = append(coordinateArray, x);
            coordinateArray = append(coordinateArray, y);
        }
    }

    public void draw() {
        background(145, 30, 45);

        fill(34, 127, 60);
        for (int i = 0; i < coordinateArray.length; i+=2)
            ellipse(coordinateArray[i] + xOffset * MULTIPLIER , coordinateArray[i+1] + yOffset * MULTIPLIER, 14, 15);
    }
}