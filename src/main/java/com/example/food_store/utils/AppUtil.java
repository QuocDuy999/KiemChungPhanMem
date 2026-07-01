package com.example.food_store.utils;

import java.util.Random;

public class AppUtil {

    private static final Random RND = new Random();

    private AppUtil() {
        // Utility class
    }

    public static String getRandomOTP() {
        int number = RND.nextInt(1000000);
        return String.format("%06d", number);
    }
}