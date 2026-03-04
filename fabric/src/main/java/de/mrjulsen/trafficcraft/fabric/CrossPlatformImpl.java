package de.mrjulsen.trafficcraft.fabric;


import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.config.ModCommonConfig;
import dev.architectury.registry.menu.MenuRegistry;
import fuzs.forgeconfigapiport.fabric.impl.core.NeoForgeConfigRegistryImpl;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.fml.config.ModConfig;

public final class CrossPlatformImpl {
    
    public static void registerConfig() {
        NeoForgeConfigRegistryImpl.INSTANCE.register(DragonLib.MODID, ModConfig.Type.COMMON, ModCommonConfig.SPEC, TrafficCraft.MOD_ID + "-common.toml");
    }
    
    @Environment(EnvType.CLIENT)
    public static <H extends AbstractContainerMenu, S extends Screen & MenuAccess<H>> void registerScreenFactory(MenuType<? extends H> type, MenuRegistry.ScreenFactory<H, S> factory) {
        MenuScreens.register(type, factory::create);
    }
}
