package de.mrjulsen.trafficcraft.forge;

import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import de.mrjulsen.trafficcraft.TrafficCraft;

@Mod(TrafficCraft.MOD_ID)
public final class TrafficCraftForge {
    public TrafficCraftForge() {
        EventBuses.registerModEventBus(TrafficCraft.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        TrafficCraft.init();
        MinecraftForge.EVENT_BUS.addListener(ClientSetup::onSalz);
    }
}
