package com.tdgame.core;

/**
 * Utility class for managing delta time calculations and game timing.
 * Provides consistent time tracking for smooth gameplay regardless of framerate.
 */
public class Time {
    private static long lastTime = System.nanoTime();
    private static double deltaTime = 0.0;
    
    /**
     * Updates the delta time calculation. Should be called once per frame.
     * @return delta time in seconds since last update
     */
    public static double update() {
        long currentTime = System.nanoTime();
        deltaTime = (currentTime - lastTime) / 1_000_000_000.0;
        lastTime = currentTime;
        return deltaTime;
    }
    
    /**
     * Gets the current delta time without updating it.
     * @return delta time in seconds
     */
    public static double getDeltaTime() {
        return deltaTime;
    }
    
    /**
     * Resets the time tracking. Useful when resuming after pause.
     */
    public static void reset() {
        lastTime = System.nanoTime();
        deltaTime = 0.0;
    }
}