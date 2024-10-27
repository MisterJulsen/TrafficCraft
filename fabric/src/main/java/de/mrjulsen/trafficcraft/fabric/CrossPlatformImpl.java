package de.mrjulsen.trafficcraft.fabric;

import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.config.ModServerConfig;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public final class CrossPlatformImpl {
    
    public static void setRenderLayer(Block block, RenderType type) { 
        BlockRenderLayerMap.INSTANCE.putBlock(block, type);
    }

    public static void registerConfig() {
        ModLoadingContext.registerConfig(TrafficCraft.MOD_ID, ModConfig.Type.SERVER, ModServerConfig.SPEC, TrafficCraft.MOD_ID + "-server.toml");
    }
}
