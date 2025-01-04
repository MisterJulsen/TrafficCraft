package de.mrjulsen.trafficcraft.fabric;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.config.ModCommonConfig;
import fuzs.forgeconfigapiport.fabric.impl.forge.ForgeConfigRegistryImpl;
import net.minecraftforge.fml.config.ModConfig;

public final class CrossPlatformImpl {
    
    public static void registerConfig() {
        ForgeConfigRegistryImpl.INSTANCE.register(DragonLib.MODID, ModConfig.Type.COMMON, ModCommonConfig.SPEC, TrafficCraft.MOD_ID + "-common.toml");
    }
}
