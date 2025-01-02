package de.mrjulsen.trafficcraft.fabric;

import net.fabricmc.api.ModInitializer;

import de.mrjulsen.trafficcraft.TrafficCraft;

public final class TrafficCraftFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        TrafficCraft.init();
    }
}
