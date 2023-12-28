package de.mrjulsen.trafficcraft;

import com.mojang.logging.LogUtils;

import de.mrjulsen.trafficcraft.client.screen.menu.ModMenuTypes;
import de.mrjulsen.trafficcraft.config.ModCommonConfig;
import de.mrjulsen.trafficcraft.init.ClientInitWrapper;
import de.mrjulsen.trafficcraft.init.ServerInit;
import de.mrjulsen.trafficcraft.network.NetworkManager;
import de.mrjulsen.trafficcraft.registry.ModBlockEntities;
import de.mrjulsen.trafficcraft.registry.ModBlocks;
import de.mrjulsen.trafficcraft.registry.ModItems;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(ModMain.MOD_ID)
public class ModMain {
    public static final String MOD_ID = "trafficcraft";

    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    public ModMain() {

        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        eventBus.addListener(ServerInit::setup);
        eventBus.addListener(ClientInitWrapper::setup);

        ModBlocks.register(eventBus);
        ModItems.register(eventBus);
        ModBlockEntities.register(eventBus);
        ModMenuTypes.register(eventBus);        
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ModCommonConfig.SPEC, MOD_ID + "-common.toml");
        NetworkManager.create();       
        MinecraftForge.EVENT_BUS.register(this);
    }
}
