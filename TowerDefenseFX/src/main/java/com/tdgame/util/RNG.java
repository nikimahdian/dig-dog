package com.tdgame.util;

import java.util.Random;

/**
 * Thread-safe random number generator utilities for game logic.
 * Provides consistent random behavior and common random operations.
 */
public class RNG {
    private static final ThreadLocal<Random> THREAD_LOCAL_RANDOM = 
        ThreadLocal.withInitial(() -> new Random());
    
    /**
     * Get the thread-local Random instance
     */
    public static Random get() {
        return THREAD_LOCAL_RANDOM.get();
    }
    
    /**
     * Random integer between min (inclusive) and max (exclusive)
     */
    public static int nextInt(int min, int max) {
        return get().nextInt(max - min) + min;
    }
    
    /**
     * Random double between min (inclusive) and max (exclusive)
     */
    public static double nextDouble(double min, double max) {
        return get().nextDouble() * (max - min) + min;
    }
    
    /**
     * Random boolean with specified probability
     * @param probability chance of returning true (0.0 to 1.0)
     */
    public static boolean nextBoolean(double probability) {
        return get().nextDouble() < probability;
    }
    
    /**
     * Random gaussian (normal distribution) with mean and standard deviation
     */
    public static double nextGaussian(double mean, double stdDev) {
        return get().nextGaussian() * stdDev + mean;
    }
    
    /**
     * Pick a random element from an array
     */
    public static <T> T choice(T[] array) {
        if (array.length == 0) return null;
        return array[get().nextInt(array.length)];
    }
    
    /**
     * Pick a random element from an int array
     */
    public static int choice(int[] array) {
        if (array.length == 0) return 0;
        return array[get().nextInt(array.length)];
    }
    
    /**
     * Shuffle an array in place
     */
    public static <T> void shuffle(T[] array) {
        Random rnd = get();
        for (int i = array.length - 1; i > 0; i--) {
            int index = rnd.nextInt(i + 1);
            T temp = array[index];
            array[index] = array[i];
            array[i] = temp;
        }
    }
}