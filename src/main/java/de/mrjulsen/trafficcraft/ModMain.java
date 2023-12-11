package de.mrjulsen.trafficcraft;

import com.mojang.logging.LogUtils;

import de.mrjulsen.mcdragonlib.setup.IProxy;
import de.mrjulsen.trafficcraft.client.screen.menu.ModMenuTypes;
import de.mrjulsen.trafficcraft.config.ModClientConfig;
import de.mrjulsen.trafficcraft.config.ModCommonConfig;
import de.mrjulsen.trafficcraft.network.NetworkManager;
import de.mrjulsen.trafficcraft.proxy.ClientProxy;
import de.mrjulsen.trafficcraft.proxy.ServerProxy;
import de.mrjulsen.trafficcraft.registry.ModBlockEntities;
import de.mrjulsen.trafficcraft.registry.ModBlocks;
import de.mrjulsen.trafficcraft.registry.ModItems;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(ModMain.MOD_ID)
public class ModMain {
    public static final String MOD_ID = "trafficcraft";
    public final IProxy PROXY = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> ServerProxy::new);

    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    public ModMain() {

        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        eventBus.addListener(this::setup);
        eventBus.addListener(this::setupClient);

        ModBlocks.register(eventBus);
        ModItems.register(eventBus);
        ModBlockEntities.register(eventBus);
        ModMenuTypes.register(eventBus);        
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ModClientConfig.SPEC, MOD_ID + "-client.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ModCommonConfig.SPEC, MOD_ID + "-common.toml");
        NetworkManager.registerNetworkPackets();        
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        LOGGER.info("Welcome to the TRAFFICCRAFT mod by MRJULSEN.");
        PROXY.setup(event);
    }

    private void setupClient(final FMLClientSetupEvent event) {
        LOGGER.info("Welcome to the TRAFFICCRAFT mod by MRJULSEN.");
        ((ClientProxy)PROXY).setupClient(event);
    }
}
