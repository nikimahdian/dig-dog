package com.tdgame.model.grid;

import com.tdgame.model.actors.Tower;
import com.tdgame.model.actors.AADefense;

/**
 * Represents a predefined slot where towers and AA defenses can be built.
 */
public class BuildSlot {
    private final int col, row;
    private final double worldX, worldY;
    private Tower tower;
    private AADefense aaDefense;
    private boolean occupied = false;
    
    public BuildSlot(int col, int row, double tileSize) {
        this.col = col;
        this.row = row;
        this.worldX = col * tileSize + tileSize / 2;
        this.worldY = row * tileSize + tileSize / 2;
    }
    
    public boolean canBuild() {
        return !occupied;
    }
    
    public void placeTower(Tower tower) {
        if (canBuild()) {
            this.tower = tower;
            this.occupied = true;
            tower.setPosition(worldX, worldY);
        }
    }
    
    public void placeAADefense(AADefense aa) {
        if (canBuild()) {
            this.aaDefense = aa;
            this.occupied = true;
            aa.setPosition(worldX, worldY);
        }
    }
    
    // Getters
    public int getCol() { return col; }
    public int getRow() { return row; }
    public double getWorldX() { return worldX; }
    public double getWorldY() { return worldY; }
    public Tower getTower() { return tower; }
    public AADefense getAADefense() { return aaDefense; }
    public boolean isOccupied() { return occupied; }
}