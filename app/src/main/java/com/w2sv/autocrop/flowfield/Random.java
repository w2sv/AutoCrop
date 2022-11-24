package com.w2sv.autocrop.flowfield;

import java.util.ArrayList;

public class Random {
    public static int randomInt(int exclusiveMax) {
        return (int) (Math.random() * exclusiveMax);
    }

    public static <T> T randomElement(ArrayList<T> array) {
        return array.get(randomInt(array.size()));
    }
}