package de.mrjulsen.trafficcraft.fabric;

import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.config.ModCommonConfig;
import fuzs.forgeconfigapiport.impl.config.ForgeConfigRegistryImpl;
import net.minecraftforge.fml.config.ModConfig;

public final class CrossPlatformImpl {
    
    public static void registerConfig() {
        ForgeConfigRegistryImpl.INSTANCE.register(TrafficCraft.MOD_ID, ModConfig.Type.COMMON, ModCommonConfig.SPEC, TrafficCraft.MOD_ID + "-common.toml");
    }
}
