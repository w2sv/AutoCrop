package com.w2sv.flowfield;

import android.util.Pair;

import java.util.HashMap;
import java.util.Map;

import processing.core.PApplet;
import processing.core.PVector;

class FlowField {
    private final int granularity;
    private final float zOffIncrement;

    private final Map<Pair<Integer, Integer>, Float> xOffCache = new HashMap<>();
    private float zOff = 0;

    public FlowField(int granularity, float zOffIncrement) {
        this.granularity = granularity;
        this.zOffIncrement = zOffIncrement;
    }

    void updateAndApplyTo(Particle[] particles, PApplet parent) {
        HashMap<Pair<Integer, Integer>, PVector> forceCash = new HashMap<>();

        for (Particle p : particles) {
            p.applyForceVector(
                getForceVector(
                    Pair.create(
                        PApplet.floor(p.pos.x / granularity) + 1,
                        PApplet.floor(p.pos.y / granularity) + 1
                    ),
                    forceCash,
                    parent
                )
            );
        }

        zOff += zOffIncrement;
    }

    private PVector getForceVector(Pair<Integer, Integer> pos, HashMap<Pair<Integer, Integer>, PVector> forceCash, PApplet parent) {
        if (forceCash.containsKey(pos))
            return forceCash.get(pos);
        PVector v = PVector.fromAngle(parent.noise(getXOff(pos), 0, zOff) * 25.132742f).normalize();
        forceCash.put(pos, v);
        return v;
    }

    private float getXOff(Pair<Integer, Integer> pos) {
        if (xOffCache.containsKey(pos))
            //noinspection ConstantConditions
            return xOffCache.get(pos);
        float xOff = pos.first * pos.second * 0.1f;
        xOffCache.put(pos, xOff);
        return xOff;
    }
}
