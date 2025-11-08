package com.w2sv.flowfield.helper;

import java.util.ArrayList;
import java.util.Set;

public class Random {
    public static int randomInt(int exclusiveMax) {
        return (int) (Math.random() * exclusiveMax);
    }

    public static <T> T randomElement(ArrayList<T> array) {
        return array.get(randomInt(array.size()));
    }

    public static <T> T randomElement(Set<T> set) {
        int index = randomInt(set.size());
        int i = 0;
        for (T element : set) {
            if (i == index) {
                return element;
            }
            i++;
        }
        throw new IllegalArgumentException("Set is empty");
    }
}
