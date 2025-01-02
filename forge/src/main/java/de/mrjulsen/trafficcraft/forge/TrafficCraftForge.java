package de.mrjulsen.trafficcraft.forge;

import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import de.mrjulsen.trafficcraft.TrafficCraft;

@Mod(TrafficCraft.MOD_ID)
public final class TrafficCraftForge {
    public TrafficCraftForge() {
        // Submit our event bus to let Architectury API register our content on the right time.
        EventBuses.registerModEventBus(TrafficCraft.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());

        // Run our common setup.
        TrafficCraft.init();
    }
}
