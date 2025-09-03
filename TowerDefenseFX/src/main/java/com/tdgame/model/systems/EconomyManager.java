package com.tdgame.model.systems;

import com.tdgame.config.GameConfig;
import com.tdgame.core.EventBus;
import javafx.application.Platform;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Thread-safe economy management system.
 * Handles money income, spending, and UI updates.
 */
public class EconomyManager {
    
    private final GameConfig config;
    private final AtomicInteger money;
    private final int incomePerSecond;
    
    private ScheduledExecutorService scheduler;
    private volatile boolean running = false;
    
    public EconomyManager(GameConfig config) {
        this.config = config;
        this.money = new AtomicInteger(config.getStartingMoney());
        this.incomePerSecond = config.getMoneyIncomePerSec();
    }
    
    /**
     * Start the income generation
     */
    public void start() {
        if (running) return;
        
        running = true;
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "EconomyManager");
            t.setDaemon(true);
            return t;
        });
        
        // Generate income every second
        scheduler.scheduleAtFixedRate(this::generateIncome, 1000, 1000, TimeUnit.MILLISECONDS);
        
        // Notify UI of initial money
        Platform.runLater(() -> 
            EventBus.getInstance().publish(new EventBus.MoneyChangedEvent(money.get()))
        );
    }
    
    /**
     * Pause income generation
     */
    public void pause() {
        running = false;
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }
    
    /**
     * Resume income generation
     */
    public void resume() {
        if (!running) {
            start();
        }
    }
    
    /**
     * Stop the economy system
     */
    public void stop() {
        running = false;
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(1, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
    
    /**
     * Generate periodic income
     */
    private void generateIncome() {
        if (!running) return;
        
        int newAmount = money.addAndGet(incomePerSecond);
        
        // Update UI on JavaFX thread
        Platform.runLater(() -> 
            EventBus.getInstance().publish(new EventBus.MoneyChangedEvent(newAmount))
        );
    }
    
    /**
     * Attempt to spend money (thread-safe)
     * @param amount amount to spend
     * @return true if successful, false if insufficient funds
     */
    public boolean spendMoney(int amount) {
        while (true) {
            int currentMoney = money.get();
            if (currentMoney < amount) {
                return false; // Insufficient funds
            }
            
            if (money.compareAndSet(currentMoney, currentMoney - amount)) {
                // Update UI on JavaFX thread
                Platform.runLater(() -> 
                    EventBus.getInstance().publish(new EventBus.MoneyChangedEvent(money.get()))
                );
                return true;
            }
            // Retry if another thread modified money between get and compareAndSet
        }
    }
    
    /**
     * Add money (for rewards, etc.)
     */
    public void addMoney(int amount) {
        int newAmount = money.addAndGet(amount);
        
        // Update UI on JavaFX thread
        Platform.runLater(() -> 
            EventBus.getInstance().publish(new EventBus.MoneyChangedEvent(newAmount))
        );
    }
    
    /**
     * Check if player can afford a cost
     */
    public boolean canAfford(int cost) {
        return money.get() >= cost;
    }
    
    /**
     * Get current money amount (thread-safe)
     */
    public int getMoney() {
        return money.get();
    }
    
    /**
     * Get income rate per second
     */
    public int getIncomePerSecond() {
        return incomePerSecond;
    }
}