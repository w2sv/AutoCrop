package com.autocrop.activities.main;

import android.graphics.Point;

import processing.core.PApplet;
import timber.log.Timber;


public class PixelField extends PApplet {
    private static final float NOISE_INC_PER_PIXEL = 0.1f;
    private static final float NOISE_INC_PER_FRAME = 0.2f;
    private static final int NOISE_SAMPLING_PIXEL_STEP = 5;

    private static Integer canvas_width, canvas_height = null;

    private float z_off = 0f;

    public static void find_canvas_dimensions(Point screen_resolution){
        canvas_width = find_divisible_canvas_dim(screen_resolution.x);
        canvas_height = find_divisible_canvas_dim(screen_resolution.y);
    }

    private static int find_divisible_canvas_dim(int screen_dimension){
        if (screen_dimension % NOISE_SAMPLING_PIXEL_STEP == 0)
            return screen_dimension;

        for (int i = 1; i < NOISE_SAMPLING_PIXEL_STEP; i++){
            if ((screen_dimension + i) % NOISE_SAMPLING_PIXEL_STEP == 0)
                return screen_dimension + i;
        }
        throw new java.lang.RuntimeException("Couldn't find appropriate canvas dimension");
    }

    public void settings(){
        size(canvas_width, canvas_height + NOISE_SAMPLING_PIXEL_STEP * 2, P2D);
    }

    public void setup(){
        draw_pixel_field();
        Timber.i("Run PixelField.setup");
    }

    public void draw() {
        draw_pixel_field();
    }

    private void draw_pixel_field(){
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