package com.example.screenshotboundremoval;

import processing.core.PApplet;

public class PixelField extends PApplet {
    static float xOffset = 0;
    static float yOffset = 0;
    float INC = (float)0.02;
    float Z_INC = (float)0.08;

    float z_off = 0;

    public void settings() {
        size(720, 1400, P2D);
    }

    public void setup() {
    }

    public void draw() {
        float y_off = 0;
        loadPixels();
        for (int y = 0; y < height; y++){
            float x_off = 0;
            for (int x = 0; x < width; x++){
                float r = noise(x_off, y_off, z_off) * 127;
                pixels[x + y * width] = color(r * 2, r / 2, r);
                x_off += INC;
            }
            y_off += INC;
        }
        updatePixels();
        z_off += Z_INC;
    }
}