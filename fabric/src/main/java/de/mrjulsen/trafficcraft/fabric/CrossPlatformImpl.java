package de.mrjulsen.trafficcraft.fabric;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.config.ModCommonConfig;
import dev.architectury.registry.menu.MenuRegistry.ScreenFactory;
import fuzs.forgeconfigapiport.fabric.impl.core.NeoForgeConfigRegistryImpl;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.config.ModConfig.Type;

public final class CrossPlatformImpl {
    
    public static void setRenderLayer(Block block, RenderType type) { 
        BlockRenderLayerMap.INSTANCE.putBlock(block, type);
    }

    public static void registerConfig() {
        NeoForgeConfigRegistryImpl.INSTANCE.register(DragonLib.MODID, Type.COMMON, ModCommonConfig.SPEC, TrafficCraft.MOD_ID + "-common.toml");
    }
    
    @Environment(EnvType.CLIENT)
    public static <H extends AbstractContainerMenu, S extends Screen & MenuAccess<H>> void registerScreenFactory(MenuType<? extends H> type, ScreenFactory<H, S> factory) {
        MenuScreens.register(type, factory::create);
    }
}
