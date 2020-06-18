package com.example.screenshotboundremoval;

import processing.core.PApplet;

public class PixelField extends PApplet {
    float INC = (float)0.1;
    float Z_INC = (float)0.2;

    float z_off = 0;

    int width_step = 4;
    int height_step = 4;

    int canvas_width;
    int canvas_height;

    private int find_canvas_dim(int screen_dim, int step){
        if (screen_dim % step == 0)
            return screen_dim;
        else{
            for (int i = 1; i < step; i++){
                if ((screen_dim + i) % step == 0)
                    return screen_dim + 1;
            }
        }
        return -1;
    }

    PixelField(int screen_width, int screen_height){
        canvas_width = find_canvas_dim(screen_width, width_step);
        canvas_height = find_canvas_dim(screen_height, height_step);
    }

    public void settings() {
        size(canvas_width, 1348, P2D);
    }

    public void draw() {
        float y_off = 0;
        loadPixels();
        for (int y = 0; y < height; y+=height_step){
            float x_off = 0;
            for (int x = 0; x < width; x+=width_step){
                float r = noise(x_off, y_off, z_off) * 127;

                for (int x_i = 0; x_i < width_step; x_i++){
                    for (int y_i = 0; y_i < height_step; y_i++){
                        pixels[x + x_i + (y + y_i) * width] = color(r * 2, r / 2, r);
                    }
                }
                x_off += INC;
            }
            y_off += INC;
        }
        updatePixels();
        z_off += Z_INC;
    }
}