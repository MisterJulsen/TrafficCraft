package de.mrjulsen.trafficcraft.neoforge;

import de.mrjulsen.trafficcraft.TrafficCraft;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(TrafficCraft.MOD_ID)
public final class TrafficCraftNeoForge {

    private static ModContainer modContainer;

    public TrafficCraftNeoForge(ModContainer container) {
        modContainer = container;
        TrafficCraft.init();
    }

    static ModContainer getModContainer() {
        return modContainer;
    }
}
