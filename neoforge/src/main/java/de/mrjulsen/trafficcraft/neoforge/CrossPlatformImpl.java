package de.mrjulsen.trafficcraft.neoforge;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.config.ModCommonConfig;
import fuzs.forgeconfigapiport.neoforge.impl.forge.ForgeConfigRegistryImpl;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.config.ModConfig;

public final class CrossPlatformImpl {
    
    public static void setRenderLayer(Block block, RenderType type) { 
    }
    
    public static void registerConfig() {
        ForgeConfigRegistryImpl.INSTANCE.register(DragonLib.MODID, ModConfig.Type.COMMON, ModCommonConfig.SPEC, TrafficCraft.MOD_ID + "-common.toml");
    }
}
