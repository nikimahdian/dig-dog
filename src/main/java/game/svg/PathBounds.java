package game.svg;

import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;

public class PathBounds {
    
    private static Scene offscreenScene;
    private static Group offscreenGroup;
    
    static {
        // Create an offscreen scene for bounds computation
        offscreenGroup = new Group();
        offscreenScene = new Scene(offscreenGroup, 1, 1);
        // Note: In a real JavaFX application, this would need to be properly initialized
        // with a stage, but for bounds computation, we can work with just the scene
    }
    
    /**
     * Compute the geometric bounds of an SVG path string.
     * This is used to determine spatial regions in the SVG for sprite extraction.
     * 
     * @param pathData The SVG path 'd' attribute string
     * @return The bounds of the path, or null if the path is invalid
     */
    public static Bounds computePathBounds(String pathData) {
        if (pathData == null || pathData.trim().isEmpty()) {
            return null;
        }
        
        try {
            // Create SVGPath node
            SVGPath svgPath = new SVGPath();
            svgPath.setContent(pathData);
            
            // Temporarily add to offscreen scene for bounds computation
            offscreenGroup.getChildren().clear();
            offscreenGroup.getChildren().add(svgPath);
            
            // Force layout to compute bounds
            offscreenScene.getRoot().applyCss();
            offscreenScene.getRoot().autosize();
            
            // Get the bounds using the implementation method as specified in the prompt
            Bounds bounds;
            try {
                // Try to use the impl method if available (as specified in prompt)
                java.lang.reflect.Method implMethod = svgPath.getClass().getMethod("impl_getGeomBounds");
                bounds = (Bounds) implMethod.invoke(svgPath);
            } catch (Exception e) {
                // Fallback to standard bounds if impl method isn't available
                bounds = svgPath.getBoundsInLocal();
            }
            
            // Clean up
            offscreenGroup.getChildren().clear();
            
            return bounds;
            
        } catch (Exception e) {
            System.err.println("Error computing bounds for path: " + pathData);
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Check if a path's bounds intersect with a given rectangular region.
     * Used for sprite extraction by spatial region.
     * 
     * @param pathData The SVG path 'd' attribute string
     * @param x Left edge of the region
     * @param y Top edge of the region  
     * @param width Width of the region
     * @param height Height of the region
     * @return true if the path intersects the region, false otherwise
     */
    public static boolean pathIntersectsRegion(String pathData, double x, double y, double width, double height) {
        Bounds pathBounds = computePathBounds(pathData);
        if (pathBounds == null) {
            return false;
        }
        
        // Check for rectangle intersection
        double pathMinX = pathBounds.getMinX();
        double pathMinY = pathBounds.getMinY();
        double pathMaxX = pathBounds.getMaxX();
        double pathMaxY = pathBounds.getMaxY();
        
        double regionMaxX = x + width;
        double regionMaxY = y + height;
        
        // No intersection if completely separated
        if (pathMaxX < x || pathMinX > regionMaxX || 
            pathMaxY < y || pathMinY > regionMaxY) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Get the center point of a path's bounds.
     * 
     * @param pathData The SVG path 'd' attribute string
     * @return Array containing [centerX, centerY], or null if bounds cannot be computed
     */
    public static double[] getPathCenter(String pathData) {
        Bounds bounds = computePathBounds(pathData);
        if (bounds == null) {
            return null;
        }
        
        double centerX = bounds.getMinX() + bounds.getWidth() / 2.0;
        double centerY = bounds.getMinY() + bounds.getHeight() / 2.0;
        
        return new double[]{centerX, centerY};
    }
    
    /**
     * Initialize the offscreen scene with a proper stage context.
     * This should be called from the JavaFX Application Thread after a Stage is available.
     * 
     * @param stage A stage to use for scene context (can be hidden)
     */
    public static void initializeWithStage(Stage stage) {
        if (offscreenScene != null && offscreenGroup != null) {
            // Re-create with proper stage context if needed
            stage.setScene(offscreenScene);
            // The scene is now properly initialized for bounds computation
        }
    }
}