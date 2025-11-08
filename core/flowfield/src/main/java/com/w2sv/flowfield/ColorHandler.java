package com.w2sv.flowfield;

import com.google.common.collect.Sets;

import java.util.ArrayList;
import java.util.Set;

import processing.core.PGraphics;

/**
 * Handles period color changing & inherent random picking, color-dependent canvas modification
 */
class ColorHandler {
    private final PeriodicalRunner runner;
    private final Set<Integer> colorPalette;
    private final float strokeAlpha;
    public int color;

    public ColorHandler(int changePeriod, Set<Integer> colorPalette, float strokeAlpha) {
        this.runner = new PeriodicalRunner(changePeriod);
        this.colorPalette = colorPalette;
        this.color = Random.randomElement(colorPalette);
        this.strokeAlpha = strokeAlpha;
    }

    public void changeColorIfDue(int millis, PGraphics canvas) {
        runner.runIfDue(millis, () -> {
            setNewRandomlyPickedColor();
            setStrokeColor(canvas);
        });
    }

    public void setStrokeColor(PGraphics canvas) {
        canvas.stroke(color, strokeAlpha);
    }

    private void setNewRandomlyPickedColor() {
        color = Random.randomElement(Sets.difference(colorPalette, Set.of(color)));
    }
}
