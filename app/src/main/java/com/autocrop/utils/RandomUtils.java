package com.autocrop.utils;

import java.util.ArrayList;

public class RandomUtils{
    public static int randomInt(int exclusiveMax){
        return (int)(Math.random() * exclusiveMax);
    }

    public static <T> T randomElement(ArrayList<T> array){
        return array.get(randomInt(array.size()));
    }
}