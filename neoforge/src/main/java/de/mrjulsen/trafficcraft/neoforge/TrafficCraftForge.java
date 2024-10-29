package de.mrjulsen.trafficcraft.neoforge;


import de.mrjulsen.trafficcraft.TrafficCraft;
import net.neoforged.fml.common.Mod;

@Mod(TrafficCraft.MOD_ID)
public final class TrafficCraftForge {
    public TrafficCraftForge() {
        TrafficCraft.init();
    }
}
