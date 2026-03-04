package de.mrjulsen.trafficcraft;

import de.mrjulsen.trafficcraft.registry.*;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import de.mrjulsen.trafficcraft.client.screen.menu.ModMenuTypes;
import de.mrjulsen.trafficcraft.data.AgingManager;
import de.mrjulsen.trafficcraft.init.ClientInitWrapper;
import de.mrjulsen.trafficcraft.init.ServerInit;
import de.mrjulsen.trafficcraft.world.ModWorldGen;
import dev.architectury.platform.Platform;
import net.fabricmc.api.EnvType;

public final class TrafficCraft {
    public static final String MOD_ID = "trafficcraft";
    public static final String MOD_NAME = "TrafficCraft";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static void init() {
        ServerInit.init();
        if (Platform.getEnv() == EnvType.CLIENT) {
            ClientInitWrapper.init();
            AgingManager.init();
        }

        ModBlocks.register();
        ModItems.register();
        ModBlockEntities.register();
        ModMenuTypes.init();
        ModNetworkManager.init();
        ModCreativeModeTab.init();
        ModWorldGen.init();
        ModTags.init();
        ModDataComponents.init();
            
        //ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ModCommonConfig.SPEC, MOD_ID + "-common.toml");
        CrossPlatform.registerConfig();
    }
}
