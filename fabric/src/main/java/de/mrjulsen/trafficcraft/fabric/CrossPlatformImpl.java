package de.mrjulsen.trafficcraft.fabric;

import java.util.function.Function;

import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.config.ModCommonConfig;
import de.mrjulsen.trafficcraft.init.ClientInit;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraftforge.api.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public final class CrossPlatformImpl {
    
    public static void registerConfig() {
        ModLoadingContext.registerConfig(TrafficCraft.MOD_ID, ModConfig.Type.COMMON, ModCommonConfig.SPEC, TrafficCraft.MOD_ID + "-common.toml");
    }
    
    @Environment(EnvType.CLIENT)
    public static <T extends TooltipComponent> void registerTooltipComponentFactory(Class<T> cls, Function<? super T, ? extends ClientTooltipComponent> factory) {
        ClientInit.registerTooltipComponentFactory(cls, factory);
    }
}
