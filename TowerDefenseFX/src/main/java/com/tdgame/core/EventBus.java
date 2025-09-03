package com.tdgame.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Simple event bus for decoupling UI and game systems.
 * Allows publishers to emit events and subscribers to listen for specific event types.
 */
public class EventBus {
    private static final EventBus INSTANCE = new EventBus();
    
    private final Map<Class<?>, List<Consumer<Object>>> listeners = new HashMap<>();
    
    private EventBus() {}
    
    public static EventBus getInstance() {
        return INSTANCE;
    }
    
    /**
     * Subscribe to events of a specific type
     * @param eventType the class of events to listen for
     * @param listener the callback to invoke when events are published
     */
    @SuppressWarnings("unchecked")
    public <T> void subscribe(Class<T> eventType, Consumer<T> listener) {
        listeners.computeIfAbsent(eventType, k -> new ArrayList<>())
                 .add((Consumer<Object>) listener);
    }
    
    /**
     * Publish an event to all registered listeners
     * @param event the event object to publish
     */
    public void publish(Object event) {
        List<Consumer<Object>> eventListeners = listeners.get(event.getClass());
        if (eventListeners != null) {
            for (Consumer<Object> listener : eventListeners) {
                listener.accept(event);
            }
        }
    }
    
    /**
     * Remove all listeners for a specific event type
     */
    public void clearListeners(Class<?> eventType) {
        listeners.remove(eventType);
    }
    
    /**
     * Remove all listeners
     */
    public void clearAllListeners() {
        listeners.clear();
    }
    
    // Common game events
    public static class MoneyChangedEvent {
        public final int newAmount;
        public MoneyChangedEvent(int newAmount) { this.newAmount = newAmount; }
    }
    
    public static class WaveStartedEvent {
        public final int waveNumber;
        public WaveStartedEvent(int waveNumber) { this.waveNumber = waveNumber; }
    }
    
    public static class EnemyReachedCastleEvent {
        public final int damage;
        public EnemyReachedCastleEvent(int damage) { this.damage = damage; }
    }
    
    public static class GameOverEvent {
        public final boolean victory;
        public GameOverEvent(boolean victory) { this.victory = victory; }
    }
}