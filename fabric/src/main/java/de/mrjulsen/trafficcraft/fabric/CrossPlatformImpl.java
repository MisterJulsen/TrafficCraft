package de.mrjulsen.trafficcraft.fabric;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.config.ModCommonConfig;
import fuzs.forgeconfigapiport.fabric.impl.forge.ForgeConfigRegistryImpl;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fml.config.ModConfig;

public final class CrossPlatformImpl {
    
    public static void setRenderLayer(Block block, RenderType type) { 
        BlockRenderLayerMap.INSTANCE.putBlock(block, type);
    }

    public static void registerConfig() {
        ForgeConfigRegistryImpl.INSTANCE.register(DragonLib.MODID, ModConfig.Type.COMMON, ModCommonConfig.SPEC, TrafficCraft.MOD_ID + "-common.toml");
    }
}
