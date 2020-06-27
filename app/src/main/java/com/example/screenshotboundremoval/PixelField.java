package com.example.screenshotboundremoval;

import processing.core.PApplet;

public class PixelField extends PApplet {
    private final float NOISE_INC_PER_PIXEL = (float)0.1;
    private final float NOISE_INC_PER_FRAME = (float)0.2;

    private final int NOISE_SAMPLING_PIXEL_STEP = 4;

    private float z_off = 0;

    private int canvas_width, canvas_height;

    private int find_divisible_canvas_dim(int screen_dim){
        if (screen_dim % NOISE_SAMPLING_PIXEL_STEP == 0)
            return screen_dim;
        else{
            for (int i = 1; i < NOISE_SAMPLING_PIXEL_STEP; i++){
                if ((screen_dim + i) % NOISE_SAMPLING_PIXEL_STEP == 0)
                    return screen_dim + i;
            }
        }
        return -1;
    }

    PixelField(int screen_width, int screen_height){
        canvas_width = find_divisible_canvas_dim(screen_width);
        canvas_height = find_divisible_canvas_dim(screen_height);
    }

    public void settings() {
        size(canvas_width, canvas_height + NOISE_SAMPLING_PIXEL_STEP * 2, P2D);
    }

    public void draw() {
        loadPixels();
        float y_off = 0;

        for (int y = 0; y < height; y+= NOISE_SAMPLING_PIXEL_STEP){
            float x_off = 0;

            for (int x = 0; x < width; x+= NOISE_SAMPLING_PIXEL_STEP){
                float base_color = noise(x_off, y_off, z_off) * 127;

                for (int x_i = 0; x_i < NOISE_SAMPLING_PIXEL_STEP; x_i++){
                    for (int y_i = 0; y_i < NOISE_SAMPLING_PIXEL_STEP; y_i++)
                        pixels[x + x_i + (y + y_i) * width] = color(base_color * 2, base_color / 2, base_color);
                }
                x_off += NOISE_INC_PER_PIXEL;
            }
            y_off += NOISE_INC_PER_PIXEL;
        }
        updatePixels();
        z_off += NOISE_INC_PER_FRAME;
    }
}