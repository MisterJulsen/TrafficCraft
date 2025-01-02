package de.mrjulsen.trafficcraft;

import java.util.List;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import de.mrjulsen.mcdragonlib.net.NetworkManagerBase;
import de.mrjulsen.trafficcraft.client.screen.menu.ModMenuTypes;
import de.mrjulsen.trafficcraft.data.AgingManager;
import de.mrjulsen.trafficcraft.init.ClientInitWrapper;
import de.mrjulsen.trafficcraft.init.ServerInit;
import de.mrjulsen.trafficcraft.network.packets.cts.ColorPaletteItemPacket;
import de.mrjulsen.trafficcraft.network.packets.cts.CreativePatternCataloguePacket;
import de.mrjulsen.trafficcraft.network.packets.cts.LinkerModePacket;
import de.mrjulsen.trafficcraft.network.packets.cts.PaintBrushPacket;
import de.mrjulsen.trafficcraft.network.packets.cts.PatternCatalogueDeletePacket;
import de.mrjulsen.trafficcraft.network.packets.cts.PatternCatalogueIndexPacket;
import de.mrjulsen.trafficcraft.network.packets.cts.PatternCatalogueIndexPacketGui;
import de.mrjulsen.trafficcraft.network.packets.cts.RoadBuilderBuildRoadPacket;
import de.mrjulsen.trafficcraft.network.packets.cts.RoadBuilderDataPacket;
import de.mrjulsen.trafficcraft.network.packets.cts.RoadBuilderResetPacket;
import de.mrjulsen.trafficcraft.network.packets.cts.StreetLampConfigPacket;
import de.mrjulsen.trafficcraft.network.packets.cts.TownSignPacket;
import de.mrjulsen.trafficcraft.network.packets.cts.TrafficLightControllerPacket;
import de.mrjulsen.trafficcraft.network.packets.cts.TrafficLightPacket;
import de.mrjulsen.trafficcraft.network.packets.cts.TrafficLightSchedulePacket;
import de.mrjulsen.trafficcraft.network.packets.cts.TrafficSignPatternPacket;
import de.mrjulsen.trafficcraft.network.packets.cts.WritableSignPacket;
import de.mrjulsen.trafficcraft.network.packets.stc.TrafficSignTextureResetPacket;
import de.mrjulsen.trafficcraft.network.packets.stc.TrafficSignWorkbenchUpdateClientPacket;
import de.mrjulsen.trafficcraft.registry.ModAccessorTypes;
import de.mrjulsen.trafficcraft.registry.ModBlockEntities;
import de.mrjulsen.trafficcraft.registry.ModBlocks;
import de.mrjulsen.trafficcraft.registry.ModItems;
import dev.architectury.networking.NetworkChannel;
import dev.architectury.platform.Platform;
import net.fabricmc.api.EnvType;

public final class TrafficCraft {
    public static final String MOD_ID = "trafficcraft";
    public static final String MOD_NAME = "TrafficCraft";
    public static final Logger LOGGER = LogUtils.getLogger();

    private static NetworkManagerBase networkManager;

    public static void init() {
        ServerInit.init();
        if (Platform.getEnv() == EnvType.CLIENT) {
            ClientInitWrapper.init();
            AgingManager.init();
        }

        ModBlocks.register();
        ModItems.register();
        ModBlockEntities.register();
        ModMenuTypes.register();
        ModAccessorTypes.init();
            
        //ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ModCommonConfig.SPEC, MOD_ID + "-common.toml");

        networkManager = new NetworkManagerBase(MOD_ID, "trafficcraft_network", List.of(
            // cts
            ColorPaletteItemPacket.class,
            CreativePatternCataloguePacket.class,
            PaintBrushPacket.class,
            PatternCatalogueDeletePacket.class,
            PatternCatalogueIndexPacket.class,
            PatternCatalogueIndexPacketGui.class,
            RoadBuilderBuildRoadPacket.class,
            RoadBuilderDataPacket.class,
            RoadBuilderResetPacket.class,
            StreetLampConfigPacket.class,
            TownSignPacket.class,
            TrafficLightControllerPacket.class,
            TrafficLightPacket.class,
            TrafficLightSchedulePacket.class,
            TrafficSignPatternPacket.class,
            WritableSignPacket.class,
            LinkerModePacket.class,

            // stc
            TrafficSignTextureResetPacket.class,
            TrafficSignWorkbenchUpdateClientPacket.class
        ));

        CrossPlatform.registerConfig();
    }

    public static final NetworkChannel net() {
        return networkManager.CHANNEL;
    }
}
