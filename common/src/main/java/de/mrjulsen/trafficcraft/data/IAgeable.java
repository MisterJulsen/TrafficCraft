package de.mrjulsen.trafficcraft.data;

public interface IAgeable {
    void onAging(int age);
    AgingType getAgingType();
    
    default void initAgeable() {
        AgingManager.add(this);
    }

    public static enum AgingType {
        RENDER,
        TICK
    }
}
