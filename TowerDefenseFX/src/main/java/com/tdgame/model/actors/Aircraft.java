package com.tdgame.model.actors;

import com.tdgame.config.Balance;
import com.tdgame.config.GameConfig;
import com.tdgame.core.EventBus;
import com.tdgame.model.systems.CombatSystem;
import com.tdgame.util.Math2D;

import java.util.List;
import java.util.ArrayList;

/**
 * Fast-moving aircraft enemy.
 * Flies straight across the map and strikes the row/column with most towers.
 */
public class Aircraft extends Enemy {
    
    private boolean hasTriggeredStrike = false;
    private boolean isFlying = true;
    private double targetX, targetY;
    private CombatSystem combatSystem;
    private GameConfig gameConfig;
    
    // Death animation properties
    private boolean isDestroying = false;
    private double destructionTime = 0.0;
    private final double destructionDuration = 1.0; // 1 second destruction animation
    private double fadeAlpha = 1.0;
    
    // Strike data
    private int strikeRow = -1;
    private int strikeCol = -1;
    private boolean isRowStrike = true; // true for row, false for column
    
    public Aircraft(Balance.EnemyStats stats) {
        super(50, stats.speed, stats.power, stats.spriteIndex); // Aircraft have some HP for AA
        this.maxHp = 50;
        this.currentHp = 50;
    }
    
    public void setCombatSystem(CombatSystem combatSystem, GameConfig gameConfig) {
        this.combatSystem = combatSystem;
        this.gameConfig = gameConfig;
        
        // Plan strike immediately
        planStrike();
    }
    
    @Override
    public void update(double deltaTime) {
        // Handle destruction animation
        if (isDestroying) {
            updateDestructionAnimation(deltaTime);
            return;
        }
        
        if (!alive) return;
        
        if (isFlying && !hasTriggeredStrike) {
            // Move straight across map instead of following path
            updateStraightFlight(deltaTime);
            
            // Trigger strike when aircraft crosses into map area
            boolean shouldStrike = false;
            
            if (isRowStrike) {
                if (x > 100) { // Strike when well inside map
                    shouldStrike = true;
                }
            } else {
                if (y > 100) { // Strike when well inside map
                    shouldStrike = true;
                }
            }
            
            if (shouldStrike) {
                triggerStrike();
                hasTriggeredStrike = true;
            }
        }
        
        // Always continue flying until off map
        if (hasTriggeredStrike) {
            updateStraightFlight(deltaTime);
            
            // Remove aircraft when it flies completely off map
            if ((isRowStrike && x > gameConfig.getGridCols() * gameConfig.getTileSize() + 100) ||
                (!isRowStrike && y > gameConfig.getGridRows() * gameConfig.getTileSize() + 100)) {
                alive = false;
                isFlying = false;
            }
        }
    }
    
    @Override
    protected void updateSpecific(double deltaTime) {
        // Aircraft update is handled in main update method
    }
    
    /**
     * Plan the strike by finding row/column with most towers
     */
    private void planStrike() {
        if (combatSystem == null) return;
        
        List<Tower> towers = combatSystem.getTowers();
        List<AADefense> aaList = combatSystem.getAADefenses();
        
        int gridCols = gameConfig.getGridCols();
        int gridRows = gameConfig.getGridRows();
        
        // Count towers in each row and column
        int[] rowCounts = new int[gridRows];
        int[] colCounts = new int[gridCols];
        
        // Count towers
        for (Tower tower : towers) {
            if (tower.isAlive()) {
                int col = (int)(tower.getX() / gameConfig.getTileSize());
                int row = (int)(tower.getY() / gameConfig.getTileSize());
                
                if (col >= 0 && col < gridCols) colCounts[col]++;
                if (row >= 0 && row < gridRows) rowCounts[row]++;
            }
        }
        
        // Count AA defenses
        for (AADefense aa : aaList) {
            if (aa.isAlive()) {
                int col = (int)(aa.getX() / gameConfig.getTileSize());
                int row = (int)(aa.getY() / gameConfig.getTileSize());
                
                if (col >= 0 && col < gridCols) colCounts[col]++;
                if (row >= 0 && row < gridRows) rowCounts[row]++;
            }
        }
        
        // Find row with most towers
        int maxRowCount = 0;
        int bestRow = gridRows / 2; // Default to middle
        for (int i = 0; i < rowCounts.length; i++) {
            if (rowCounts[i] > maxRowCount) {
                maxRowCount = rowCounts[i];
                bestRow = i;
            }
        }
        
        // Find column with most towers
        int maxColCount = 0;
        int bestCol = gridCols / 2; // Default to middle
        for (int i = 0; i < colCounts.length; i++) {
            if (colCounts[i] > maxColCount) {
                maxColCount = colCounts[i];
                bestCol = i;
            }
        }
        
        // Choose row or column strike based on which has more towers
        if (maxRowCount >= maxColCount && maxRowCount > 0) {
            isRowStrike = true;
            strikeRow = bestRow;
            // Fly from left to right across the row
            x = -100;
            y = strikeRow * gameConfig.getTileSize() + gameConfig.getTileSize() / 2;
            targetX = gridCols * gameConfig.getTileSize() + 100;
            targetY = y;
            
        } else if (maxColCount > 0) {
            isRowStrike = false;
            strikeCol = bestCol;
            // Fly from top to bottom down the column
            x = strikeCol * gameConfig.getTileSize() + gameConfig.getTileSize() / 2;
            y = -100;
            targetX = x;
            targetY = gridRows * gameConfig.getTileSize() + 100;
            
        } else {
            // No targets found, fly across center
            isRowStrike = true;
            strikeRow = gridRows / 2;
            x = -100;
            y = strikeRow * gameConfig.getTileSize() + gameConfig.getTileSize() / 2;
            targetX = gridCols * gameConfig.getTileSize() + 100;
            targetY = y;
            
        }
        
        // Set initial position
        setPosition(x, y);
    }
    
    /**
     * Update straight flight movement
     */
    private void updateStraightFlight(double deltaTime) {
        double angle = Math2D.angle(x, y, targetX, targetY);
        double moveDistance = baseSpeed * 120 * deltaTime; // Aircraft fly fast
        
        x += Math.cos(angle) * moveDistance;
        y += Math.sin(angle) * moveDistance;
    }
    
    /**
     * Trigger row/column strike
     */
    private void triggerStrike() {
        if (combatSystem == null) return;
        
        int damage = 80; // High aircraft strike damage!
        
        
        if (isRowStrike) {
            // Strike entire row
            strikeRow(strikeRow, damage);
        } else {
            // Strike entire column
            strikeColumn(strikeCol, damage);
        }
        
        // Aircraft continues flying off map (don't set isFlying = false)
    }
    
    /**
     * Strike all towers and AA in a row
     */
    private void strikeRow(int row, int damage) {
        List<Tower> towers = combatSystem.getTowers();
        List<AADefense> aaList = combatSystem.getAADefenses();
        
        double tileSize = gameConfig.getTileSize();
        double rowY = row * tileSize + tileSize/2;
        
        int towersHit = 0;
        int aaHit = 0;
        
        // Strike towers in this row
        for (Tower tower : towers) {
            if (tower.isAlive()) {
                int towerRow = (int)(tower.getY() / tileSize);
                if (towerRow == row) {
                    tower.takeDamage(damage);
                    towersHit++;
                }
            }
        }
        
        // Strike AA defenses in this row
        for (AADefense aa : aaList) {
            if (aa.isAlive()) {
                int aaRow = (int)(aa.getY() / tileSize);
                if (aaRow == row) {
                    aa.takeDamage(damage);
                    aaHit++;
                }
            }
        }
        
        System.out.println("🚀 Row " + row + " strike complete! Hit " + towersHit + " towers, " + aaHit + " AA defenses.");
    }
    
    /**
     * Strike all towers and AA in a column
     */
    private void strikeColumn(int col, int damage) {
        List<Tower> towers = combatSystem.getTowers();
        List<AADefense> aaList = combatSystem.getAADefenses();
        
        double tileSize = gameConfig.getTileSize();
        double colX = col * tileSize + tileSize/2;
        
        int towersHit = 0;
        int aaHit = 0;
        
        // Strike towers in this column
        for (Tower tower : towers) {
            if (tower.isAlive()) {
                int towerCol = (int)(tower.getX() / tileSize);
                if (towerCol == col) {
                    tower.takeDamage(damage);
                    towersHit++;
                }
            }
        }
        
        // Strike AA defenses in this column
        for (AADefense aa : aaList) {
            if (aa.isAlive()) {
                int aaCol = (int)(aa.getX() / tileSize);
                if (aaCol == col) {
                    aa.takeDamage(damage);
                    aaHit++;
                }
            }
        }
        
        System.out.println("🚀 Column " + col + " strike complete! Hit " + towersHit + " towers, " + aaHit + " AA defenses.");
    }
    
    @Override
    public void setPath(com.tdgame.model.grid.GridMap.Path path) {
        // Aircraft don't follow paths - they fly straight
    }
    
    @Override
    protected void onTakeDamage(int damage) {
        // Aircraft are fragile - any hit kills them
        currentHp = 0;
        startDestructionAnimation();
    }
    
    @Override
    protected void onDeath() {
        // Aircraft shot down - no strike triggered if killed before midpoint
        hasTriggeredStrike = true; // Prevent strike if shot down
    }
    
    /**
     * Start destruction animation instead of immediate death
     */
    private void startDestructionAnimation() {
        isDestroying = true;
        destructionTime = 0.0;
        fadeAlpha = 1.0;
        hasTriggeredStrike = true; // Prevent strike
    }
    
    /**
     * Update destruction animation (spinning, fading, falling)
     */
    private void updateDestructionAnimation(double deltaTime) {
        destructionTime += deltaTime;
        
        // Fade out over time
        fadeAlpha = 1.0 - (destructionTime / destructionDuration);
        
        // Aircraft falls down while spinning
        y += 150 * deltaTime; // Fall speed
        
        // Remove aircraft when animation completes
        if (destructionTime >= destructionDuration) {
            alive = false;
            isDestroying = false;
        }
    }
    
    @Override
    protected void onReachedEnd() {
        // Aircraft don't damage castle directly, but their power counts for victory conditions
        EventBus.getInstance().publish(new EventBus.EnemyReachedCastleEvent(0)); // No direct damage
    }
    
    /**
     * Check if this aircraft can be targeted by towers
     */
    @Override
    public boolean canBeTargetedByTowers() {
        return false; // Only AA can target aircraft
    }
    
    /**
     * Check if this aircraft can be targeted by AA defenses
     */
    public boolean canBeTargetedByAA() {
        return true;
    }
    
    public boolean hasTriggeredStrike() {
        return hasTriggeredStrike;
    }
    
    public void setTriggeredStrike(boolean triggered) {
        this.hasTriggeredStrike = triggered;
    }
    
    public boolean isRowStrike() { return isRowStrike; }
    public int getStrikeRow() { return strikeRow; }
    public int getStrikeCol() { return strikeCol; }
    public boolean isDestroying() { return isDestroying; }
    public double getFadeAlpha() { return fadeAlpha; }
    public double getDestructionProgress() { return destructionTime / destructionDuration; }
}