package com.tdgame.util;

/**
 * 2D math utilities for game calculations including distance, angles, and vector operations.
 */
public class Math2D {
    
    /**
     * Calculate distance between two points
     */
    public static double distance(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    /**
     * Calculate squared distance (faster when you only need comparison)
     */
    public static double distanceSquared(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return dx * dx + dy * dy;
    }
    
    /**
     * Calculate angle between two points in radians
     */
    public static double angle(double x1, double y1, double x2, double y2) {
        return Math.atan2(y2 - y1, x2 - x1);
    }
    
    /**
     * Normalize angle to [0, 2*PI] range
     */
    public static double normalizeAngle(double angle) {
        while (angle < 0) angle += 2 * Math.PI;
        while (angle >= 2 * Math.PI) angle -= 2 * Math.PI;
        return angle;
    }
    
    /**
     * Linear interpolation between two values
     */
    public static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }
    
    /**
     * Clamp a value between min and max
     */
    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
    
    /**
     * Check if a point is within a circle
     */
    public static boolean pointInCircle(double px, double py, double cx, double cy, double radius) {
        return distanceSquared(px, py, cx, cy) <= radius * radius;
    }
    
    /**
     * Simple 2D Point class
     */
    public static class Point {
        public final double x, y;
        
        public Point(double x, double y) {
            this.x = x;
            this.y = y;
        }
        
        public double distanceTo(Point other) {
            return Math2D.distance(x, y, other.x, other.y);
        }
        
        @Override
        public String toString() {
            return String.format("(%.2f, %.2f)", x, y);
        }
    }
}