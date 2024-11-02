package de.mrjulsen.trafficcraft.neoforge;

import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.config.ModCommonConfig;
import dev.architectury.registry.menu.MenuRegistry.ScreenFactory;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.config.ModConfig;

public final class CrossPlatformImpl {
    
    public static void setRenderLayer(Block block, RenderType type) { 
    }
    
    public static void registerConfig() {
        TrafficCraftNeoForge.getModContainer().registerConfig(ModConfig.Type.COMMON, ModCommonConfig.SPEC, TrafficCraft.MOD_ID + "-common.toml");
    }

    @OnlyIn(Dist.CLIENT)
    public static <H extends AbstractContainerMenu, S extends Screen & MenuAccess<H>> void registerScreenFactory(MenuType<? extends H> type, ScreenFactory<H, S> factory) {
    }
}
