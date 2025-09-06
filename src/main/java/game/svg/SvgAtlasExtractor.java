package game.svg;

import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SvgAtlasExtractor {
    
    private final Document svgDocument;
    private final Map<String, List<SvgElement>> spriteGroups;
    
    public static class SvgElement {
        public final String pathData;
        public final String fill;
        public final String transform;
        
        public SvgElement(String pathData, String fill, String transform) {
            this.pathData = pathData;
            this.fill = fill != null ? fill : "#000000";
            this.transform = transform;
        }
    }
    
    public SvgAtlasExtractor(String svgFilePath) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        this.svgDocument = builder.parse(new File(svgFilePath));
        this.spriteGroups = new HashMap<>();
        
        extractSprites();
    }
    
    private void extractSprites() {
        NodeList groups = svgDocument.getElementsByTagName("g");
        
        // Extract sprite groups by analyzing spatial regions
        extractSpriteGroup("tile.grass", 0, 0, 100, 100);
        extractSpriteGroup("tile.dirt", 100, 0, 100, 100);
        extractSpriteGroup("tower.fast", 200, 0, 100, 100);
        extractSpriteGroup("tower.heavy", 300, 0, 100, 100);
        extractSpriteGroup("enemy.soldier", 400, 0, 100, 100);
        extractSpriteGroup("enemy.tank", 500, 0, 100, 100);
        extractSpriteGroup("enemy.plane", 600, 0, 100, 100);
        extractSpriteGroup("ui.wrench", 700, 0, 50, 50);
        extractSpriteGroup("decoration.rock", 800, 0, 50, 50);
        extractSpriteGroup("decoration.bush", 850, 0, 50, 50);
        extractSpriteGroup("projectile", 900, 0, 20, 20);
        
        // If specific sprites aren't found, create fallbacks
        createFallbackSprites();
    }
    
    private void extractSpriteGroup(String spriteName, double x, double y, double width, double height) {
        List<SvgElement> elements = new ArrayList<>();
        NodeList paths = svgDocument.getElementsByTagName("path");
        
        for (int i = 0; i < paths.getLength(); i++) {
            Element pathElement = (Element) paths.item(i);
            String pathData = pathElement.getAttribute("d");
            String fill = pathElement.getAttribute("fill");
            String transform = pathElement.getAttribute("transform");
            
            if (pathData != null && !pathData.isEmpty()) {
                // For now, collect all paths - in a real implementation we'd
                // parse the path data to check if it's within the bounding box
                elements.add(new SvgElement(pathData, fill, transform));
                
                // Limit the number of elements per sprite for performance
                if (elements.size() >= 5) break;
            }
        }
        
        if (!elements.isEmpty()) {
            spriteGroups.put(spriteName, elements);
        }
    }
    
    private void createFallbackSprites() {
        // Create simple fallback sprites if extraction fails
        Map<String, String> fallbackPaths = new HashMap<>();
        
        // Simple square for grass tile
        fallbackPaths.put("tile.grass", "M 0 0 L 64 0 L 64 64 L 0 64 Z");
        
        // Rounded rectangle for dirt path
        fallbackPaths.put("tile.dirt", "M 10 0 L 54 0 Q 64 0 64 10 L 64 54 Q 64 64 54 64 L 10 64 Q 0 64 0 54 L 0 10 Q 0 0 10 0 Z");
        
        // Triangle for fast tower
        fallbackPaths.put("tower.fast", "M 32 10 L 54 50 L 10 50 Z");
        
        // Square for heavy tower
        fallbackPaths.put("tower.heavy", "M 16 16 L 48 16 L 48 48 L 16 48 Z");
        
        // Circle for soldier
        fallbackPaths.put("enemy.soldier", "M 32 32 m -16 0 a 16 16 0 1 0 32 0 a 16 16 0 1 0 -32 0");
        
        // Rectangle for tank
        fallbackPaths.put("enemy.tank", "M 8 20 L 56 20 L 56 44 L 8 44 Z");
        
        // Diamond for plane
        fallbackPaths.put("enemy.plane", "M 32 8 L 48 32 L 32 56 L 16 32 Z");
        
        // Small wrench icon
        fallbackPaths.put("ui.wrench", "M 16 10 L 48 10 L 48 16 L 42 16 L 42 48 L 36 48 L 36 16 L 28 16 L 28 48 L 22 48 L 22 16 L 16 16 Z");
        
        // Rock decoration
        fallbackPaths.put("decoration.rock", "M 16 32 Q 16 16 32 16 Q 48 16 48 32 Q 48 48 32 48 Q 16 48 16 32");
        
        // Bush decoration
        fallbackPaths.put("decoration.bush", "M 10 40 Q 10 20 20 20 Q 30 10 40 20 Q 50 20 50 30 Q 60 30 60 40 L 10 40");
        
        // Small circle for projectile
        fallbackPaths.put("projectile", "M 10 10 m -4 0 a 4 4 0 1 0 8 0 a 4 4 0 1 0 -8 0");
        
        for (Map.Entry<String, String> entry : fallbackPaths.entrySet()) {
            String spriteName = entry.getKey();
            String pathData = entry.getValue();
            
            if (!spriteGroups.containsKey(spriteName) || spriteGroups.get(spriteName).isEmpty()) {
                List<SvgElement> fallbackElements = new ArrayList<>();
                String fill = getFallbackColor(spriteName);
                fallbackElements.add(new SvgElement(pathData, fill, null));
                spriteGroups.put(spriteName, fallbackElements);
            }
        }
    }
    
    private String getFallbackColor(String spriteName) {
        if (spriteName.contains("grass")) return "#228B22";
        if (spriteName.contains("dirt")) return "#8B4513";
        if (spriteName.contains("tower")) return "#696969";
        if (spriteName.contains("soldier")) return "#4169E1";
        if (spriteName.contains("tank")) return "#2F4F4F";
        if (spriteName.contains("plane")) return "#800080";
        if (spriteName.contains("wrench")) return "#C0C0C0";
        if (spriteName.contains("rock")) return "#A0A0A0";
        if (spriteName.contains("bush")) return "#008000";
        if (spriteName.contains("projectile")) return "#FFD700";
        return "#000000";
    }
    
    public Image createSprite(String spriteName, double width, double height) {
        List<SvgElement> elements = spriteGroups.get(spriteName);
        if (elements == null || elements.isEmpty()) {
            return createSolidColorSprite(Color.MAGENTA, width, height); // Error indicator
        }
        
        Group group = new Group();
        
        for (SvgElement element : elements) {
            SVGPath svgPath = new SVGPath();
            svgPath.setContent(element.pathData);
            
            // Parse and apply fill color
            Paint fill = parseColor(element.fill);
            svgPath.setFill(fill);
            
            // Apply transforms if present
            if (element.transform != null && !element.transform.isEmpty()) {
                // Simple transform parsing - in a real implementation you'd
                // properly parse SVG transforms
            }
            
            group.getChildren().add(svgPath);
        }
        
        // Scale the group to fit the desired size
        group.setScaleX(width / 64.0);
        group.setScaleY(height / 64.0);
        
        // Create snapshot
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        WritableImage image = group.snapshot(params, null);
        
        return image;
    }
    
    private Paint parseColor(String colorString) {
        if (colorString == null || colorString.isEmpty()) {
            return Color.BLACK;
        }
        
        try {
            if (colorString.startsWith("#")) {
                return Color.web(colorString);
            } else if (colorString.startsWith("rgb")) {
                return Color.web(colorString);
            } else {
                // Named color
                return Color.web(colorString);
            }
        } catch (Exception e) {
            return Color.BLACK;
        }
    }
    
    private Image createSolidColorSprite(Color color, double width, double height) {
        Group group = new Group();
        SVGPath rect = new SVGPath();
        rect.setContent(String.format("M 0 0 L %.0f 0 L %.0f %.0f L 0 %.0f Z", width, width, height, height));
        rect.setFill(color);
        group.getChildren().add(rect);
        
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        return group.snapshot(params, null);
    }
    
    public boolean hasSprite(String spriteName) {
        return spriteGroups.containsKey(spriteName) && !spriteGroups.get(spriteName).isEmpty();
    }
}