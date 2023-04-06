package org.makingstan;

import java.util.Random;

public class Utils {
    private static Random random = new Random();
    public static int random(int min, int max)
    {
        return random.nextInt(max + 1 - min) + min;
    }
}
