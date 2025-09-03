package com.tdgame.model.systems;

import com.tdgame.config.GameConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test the thread-safe economy manager
 */
public class EconomyManagerTest {
    
    private EconomyManager economyManager;
    private GameConfig config;
    
    @BeforeEach
    void setUp() {
        // Create a minimal config for testing
        config = GameConfig.load("level1", "easy");
        economyManager = new EconomyManager(config);
    }
    
    @AfterEach
    void tearDown() {
        if (economyManager != null) {
            economyManager.stop();
        }
    }
    
    @Test
    void testInitialMoney() {
        assertEquals(config.getStartingMoney(), economyManager.getMoney());
    }
    
    @Test
    void testSpendMoney() {
        int initialMoney = economyManager.getMoney();
        int spendAmount = 50;
        
        assertTrue(economyManager.spendMoney(spendAmount));
        assertEquals(initialMoney - spendAmount, economyManager.getMoney());
    }
    
    @Test
    void testInsufficientFunds() {
        int initialMoney = economyManager.getMoney();
        int spendAmount = initialMoney + 100;
        
        assertFalse(economyManager.spendMoney(spendAmount));
        assertEquals(initialMoney, economyManager.getMoney());
    }
    
    @Test
    void testAddMoney() {
        int initialMoney = economyManager.getMoney();
        int addAmount = 75;
        
        economyManager.addMoney(addAmount);
        assertEquals(initialMoney + addAmount, economyManager.getMoney());
    }
    
    @Test
    void testCanAfford() {
        int currentMoney = economyManager.getMoney();
        
        assertTrue(economyManager.canAfford(currentMoney));
        assertTrue(economyManager.canAfford(currentMoney - 1));
        assertFalse(economyManager.canAfford(currentMoney + 1));
    }
    
    @Test
    void testConcurrentSpending() throws InterruptedException {
        int initialMoney = economyManager.getMoney();
        int numThreads = 10;
        int spendPerThread = 10;
        
        Thread[] threads = new Thread[numThreads];
        boolean[] results = new boolean[numThreads];
        
        // Create threads that try to spend money concurrently
        for (int i = 0; i < numThreads; i++) {
            final int threadIndex = i;
            threads[i] = new Thread(() -> {
                results[threadIndex] = economyManager.spendMoney(spendPerThread);
            });
        }
        
        // Start all threads
        for (Thread thread : threads) {
            thread.start();
        }
        
        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }
        
        // Count successful transactions
        int successfulTransactions = 0;
        for (boolean result : results) {
            if (result) successfulTransactions++;
        }
        
        // Verify that money spent equals successful transactions
        int expectedMoney = initialMoney - (successfulTransactions * spendPerThread);
        assertEquals(expectedMoney, economyManager.getMoney());
    }
}