package com.tdgame.model.systems;

import com.tdgame.config.GameConfig;
import com.tdgame.model.actors.*;
import com.tdgame.model.grid.GridMap;
import com.tdgame.util.Math2D;

import java.util.List;
import java.util.ArrayList;

/**
 * Manages aircraft strikes that damage player structures in rows/columns.
 * Calculates optimal strike positions to maximize damage to player defenses.
 */
public class AircraftStrikeSystem {
    
    private final GameConfig config;
    private final GridMap gridMap;
    private final CombatSystem combatSystem;
    
    private List<PendingStrike> pendingStrikes = new ArrayList<>();
    
    public AircraftStrikeSystem(GameConfig config, GridMap gridMap, CombatSystem combatSystem) {
        this.config = config;
        this.gridMap = gridMap;
        this.combatSystem = combatSystem;
    }
    
    public void update(double deltaTime) {
        // Check for aircraft ready to strike
        checkForAircraftStrikes();
        
        // Update pending strikes
        updatePendingStrikes(deltaTime);
    }
    
    /**
     * Check if any aircraft are ready to trigger strikes
     */
    private void checkForAircraftStrikes() {
        for (Enemy enemy : combatSystem.getEnemies()) {
            if (enemy instanceof Aircraft aircraft) {
                if (aircraft.isAlive() && !aircraft.hasTriggeredStrike() && 
                    aircraft.getPathProgress() >= 0.5) {
                    
                    triggerOptimalStrike(aircraft);
                    aircraft.setTriggeredStrike(true);
                }
            }
        }
    }
    
    /**
     * Trigger the optimal strike position for maximum damage
     */
    private void triggerOptimalStrike(Aircraft aircraft) {
        StrikeTarget bestTarget = calculateOptimalStrike();
        
        if (bestTarget != null) {
            PendingStrike strike = new PendingStrike(
                bestTarget.type,
                bestTarget.index,
                1.0, // 1 second warning time
                calculateStrikeDamage()
            );
            
            pendingStrikes.add(strike);
            
            // Visual/audio warning could be triggered here
            System.out.println("Aircraft strike incoming on " + bestTarget.type + " " + bestTarget.index + "!");
        }
    }
    
    /**
     * Calculate the optimal strike position
     */
    private StrikeTarget calculateOptimalStrike() {
        StrikeTarget bestTarget = null;
        double bestScore = 0;
        
        int rows = config.getGridRows();
        int cols = config.getGridCols();
        
        // Evaluate each row
        for (int row = 0; row < rows; row++) {
            double score = calculateRowStrikeValue(row);
            if (score > bestScore) {
                bestScore = score;
                bestTarget = new StrikeTarget(StrikeType.ROW, row);
            }
        }
        
        // Evaluate each column
        for (int col = 0; col < cols; col++) {
            double score = calculateColumnStrikeValue(col);
            if (score > bestScore) {
                bestScore = score;
                bestTarget = new StrikeTarget(StrikeType.COLUMN, col);
            }
        }
        
        return bestTarget;
    }
    
    /**
     * Calculate damage potential for striking a specific row
     */
    private double calculateRowStrikeValue(int row) {
        double score = 0;
        double tileSize = config.getTileSize();
        double rowY = row * tileSize + tileSize / 2;
        
        // Check towers in this row
        for (Tower tower : combatSystem.getTowers()) {
            if (tower.isAlive()) {
                Math2D.Point gridPos = gridMap.worldToGrid(tower.getX(), tower.getY());
                if (Math.abs(gridPos.y - row) < 0.5) {
                    score += tower.getMaxHp() * 2; // Towers are valuable targets
                }
            }
        }
        
        // Check AA defenses in this row
        for (AADefense aa : combatSystem.getAADefenses()) {
            if (aa.isAlive()) {
                Math2D.Point gridPos = gridMap.worldToGrid(aa.getX(), aa.getY());
                if (Math.abs(gridPos.y - row) < 0.5) {
                    score += aa.getMaxHp() * 3; // AA defenses are high priority
                }
            }
        }
        
        return score;
    }
    
    /**
     * Calculate damage potential for striking a specific column
     */
    private double calculateColumnStrikeValue(int col) {
        double score = 0;
        double tileSize = config.getTileSize();
        double colX = col * tileSize + tileSize / 2;
        
        // Check towers in this column
        for (Tower tower : combatSystem.getTowers()) {
            if (tower.isAlive()) {
                Math2D.Point gridPos = gridMap.worldToGrid(tower.getX(), tower.getY());
                if (Math.abs(gridPos.x - col) < 0.5) {
                    score += tower.getMaxHp() * 2;
                }
            }
        }
        
        // Check AA defenses in this column
        for (AADefense aa : combatSystem.getAADefenses()) {
            if (aa.isAlive()) {
                Math2D.Point gridPos = gridMap.worldToGrid(aa.getX(), aa.getY());
                if (Math.abs(gridPos.x - col) < 0.5) {
                    score += aa.getMaxHp() * 3;
                }
            }
        }
        
        return score;
    }
    
    /**
     * Update pending strikes and execute when ready
     */
    private void updatePendingStrikes(double deltaTime) {
        pendingStrikes.removeIf(strike -> {
            strike.warningTime -= deltaTime;
            
            if (strike.warningTime <= 0) {
                executeStrike(strike);
                return true; // Remove from list
            }
            
            return false;
        });
    }
    
    /**
     * Execute the aircraft strike
     */
    private void executeStrike(PendingStrike strike) {
        if (strike.type == StrikeType.ROW) {
            executeRowStrike(strike.index, strike.damage);
        } else {
            executeColumnStrike(strike.index, strike.damage);
        }
        
        System.out.println("Aircraft strike executed on " + strike.type + " " + strike.index + 
                          " dealing " + strike.damage + " damage!");
    }
    
    /**
     * Execute strike on a row
     */
    private void executeRowStrike(int row, int damage) {
        // Damage all towers and AA in this row
        for (Tower tower : combatSystem.getTowers()) {
            if (tower.isAlive()) {
                Math2D.Point gridPos = gridMap.worldToGrid(tower.getX(), tower.getY());
                if (Math.abs(gridPos.y - row) < 0.5) {
                    tower.takeDamage(damage);
                }
            }
        }
        
        for (AADefense aa : combatSystem.getAADefenses()) {
            if (aa.isAlive()) {
                Math2D.Point gridPos = gridMap.worldToGrid(aa.getX(), aa.getY());
                if (Math.abs(gridPos.y - row) < 0.5) {
                    aa.takeDamage(damage);
                }
            }
        }
    }
    
    /**
     * Execute strike on a column
     */
    private void executeColumnStrike(int col, int damage) {
        // Damage all towers and AA in this column
        for (Tower tower : combatSystem.getTowers()) {
            if (tower.isAlive()) {
                Math2D.Point gridPos = gridMap.worldToGrid(tower.getX(), tower.getY());
                if (Math.abs(gridPos.x - col) < 0.5) {
                    tower.takeDamage(damage);
                }
            }
        }
        
        for (AADefense aa : combatSystem.getAADefenses()) {
            if (aa.isAlive()) {
                Math2D.Point gridPos = gridMap.worldToGrid(aa.getX(), aa.getY());
                if (Math.abs(gridPos.x - col) < 0.5) {
                    aa.takeDamage(damage);
                }
            }
        }
    }
    
    /**
     * Calculate base strike damage
     */
    private int calculateStrikeDamage() {
        return 50; // Base aircraft strike damage
    }
    
    public List<PendingStrike> getPendingStrikes() {
        return pendingStrikes;
    }
    
    // Helper classes
    public enum StrikeType {
        ROW, COLUMN
    }
    
    private static class StrikeTarget {
        final StrikeType type;
        final int index;
        
        StrikeTarget(StrikeType type, int index) {
            this.type = type;
            this.index = index;
        }
    }
    
    public static class PendingStrike {
        final StrikeType type;
        final int index;
        double warningTime;
        final int damage;
        
        public PendingStrike(StrikeType type, int index, double warningTime, int damage) {
            this.type = type;
            this.index = index;
            this.warningTime = warningTime;
            this.damage = damage;
        }
        
        public StrikeType getType() { return type; }
        public int getIndex() { return index; }
        public double getWarningTime() { return warningTime; }
        public int getDamage() { return damage; }
    }
}