package com.tdgame.model.systems;

import com.tdgame.config.GameConfig;
import com.tdgame.core.EventBus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test the Rules system for victory/defeat conditions
 */
public class RulesTest {
    
    @Mock
    private WaveManager mockWaveManager;
    
    @Mock 
    private CombatSystem mockCombatSystem;
    
    private GameConfig config;
    private Rules rules;
    private AutoCloseable mockCloseable;
    
    @BeforeEach
    void setUp() {
        mockCloseable = MockitoAnnotations.openMocks(this);
        config = GameConfig.load("level1", "easy");
        rules = new Rules(config, mockWaveManager, mockCombatSystem);
        
        // Clear any existing event listeners
        EventBus.getInstance().clearAllListeners();
    }
    
    @AfterEach
    void tearDown() throws Exception {
        if (mockCloseable != null) {
            mockCloseable.close();
        }
        EventBus.getInstance().clearAllListeners();
    }
    
    @Test
    void testInitialState() {
        assertFalse(rules.isGameOver());
        assertFalse(rules.isVictory());
        assertEquals(0.0, rules.getCurrentLeakPercentage());
    }
    
    @Test
    void testVictoryCondition() {
        // Mock victory conditions: all waves complete and no live enemies
        when(mockWaveManager.areAllWavesComplete()).thenReturn(true);
        when(mockWaveManager.hasLiveEnemies()).thenReturn(false);
        
        rules.update(0.016); // Simulate one frame update
        
        assertTrue(rules.isGameOver());
        assertTrue(rules.isVictory());
    }
    
    @Test
    void testDefeatByLeakage() {
        // Add some enemy power first
        rules.addEnemyPower(100);
        
        // Simulate enemies reaching castle with >10% total power
        EventBus.getInstance().publish(new EventBus.EnemyReachedCastleEvent(15));
        
        rules.update(0.016);
        
        assertTrue(rules.isGameOver());
        assertFalse(rules.isVictory());
    }
    
    @Test
    void testLeakageCalculation() {
        rules.addEnemyPower(200);
        
        // Leak 30 power (15%)
        EventBus.getInstance().publish(new EventBus.EnemyReachedCastleEvent(30));
        
        assertEquals(0.15, rules.getCurrentLeakPercentage(), 0.01);
        assertEquals(30, rules.getLeakedEnemyPower());
        assertEquals(200, rules.getTotalEnemyPower());
    }
    
    @Test
    void testLeakageThreshold() {
        double threshold = config.getLeakDefeatThreshold();
        assertEquals(0.1, threshold, 0.001); // Should be 10%
        
        rules.addEnemyPower(100);
        
        // Leak exactly at threshold
        EventBus.getInstance().publish(new EventBus.EnemyReachedCastleEvent(10));
        rules.update(0.016);
        
        assertTrue(rules.isGameOver());
        assertFalse(rules.isVictory());
    }
    
    @Test
    void testRemainingLeakAllowance() {
        rules.addEnemyPower(100);
        
        // Leak 5% (5 out of 100)
        EventBus.getInstance().publish(new EventBus.EnemyReachedCastleEvent(5));
        
        double remaining = rules.getRemainingLeakAllowance();
        assertEquals(0.05, remaining, 0.01); // Should have 5% remaining before defeat
    }
    
    @Test
    void testReset() {
        // Set up some game state
        rules.addEnemyPower(100);
        EventBus.getInstance().publish(new EventBus.EnemyReachedCastleEvent(5));
        
        // Reset and verify clean state
        rules.reset();
        
        assertFalse(rules.isGameOver());
        assertFalse(rules.isVictory());
        assertEquals(0, rules.getTotalEnemyPower());
        assertEquals(0, rules.getLeakedEnemyPower());
        assertEquals(0.0, rules.getCurrentLeakPercentage());
    }
    
    @Test
    void testNoDefeatWithoutWaves() {
        // Even with waves complete, shouldn't win if enemies are still alive
        when(mockWaveManager.areAllWavesComplete()).thenReturn(true);
        when(mockWaveManager.hasLiveEnemies()).thenReturn(true);
        
        rules.update(0.016);
        
        assertFalse(rules.isGameOver());
        assertFalse(rules.isVictory());
    }
}