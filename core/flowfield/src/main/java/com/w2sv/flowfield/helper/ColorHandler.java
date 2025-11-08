package com.w2sv.flowfield.helper;

import com.google.common.collect.Sets;

import java.util.Set;
import java.util.function.Supplier;

import processing.core.PGraphics;

/**
 * Handles period color changing & inherent random picking, color-dependent canvas modification
 */
public class ColorHandler {
    private final PeriodicalRunner runner;
    private final Set<Integer> colorPalette;
    private final float strokeAlpha;
    public int color;

    public ColorHandler(int changePeriod, Set<Integer> colorPalette, float strokeAlpha, Supplier<PGraphics> canvasSupplier) {
        this.runner = new PeriodicalRunner(changePeriod, () -> {
            setNewRandomlyPickedColor();
            setStrokeColor(canvasSupplier.get());
        });
        this.colorPalette = colorPalette;
        this.color = Random.randomElement(colorPalette);
        this.strokeAlpha = strokeAlpha;
    }

    public void changeColorIfPeriodElapsed(int millis) {
        runner.runIfPeriodElapsed(millis);
    }

    public void setStrokeColor(PGraphics canvas) {
        canvas.stroke(color, strokeAlpha);
    }

    private void setNewRandomlyPickedColor() {
        color = Random.randomElement(Sets.difference(colorPalette, Set.of(color)));
    }
}
