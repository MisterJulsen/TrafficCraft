package de.mrjulsen.trafficcraft;

import java.util.List;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import de.mrjulsen.mcdragonlib.net.DLNetworkManager;
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
import de.mrjulsen.trafficcraft.registry.ModCreativeModeTab;
import de.mrjulsen.trafficcraft.registry.ModDataComponents;
import de.mrjulsen.trafficcraft.registry.ModItems;
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
        ModAccessorTypes.init();
        ModCreativeModeTab.init();
        ModWorldGen.init();
        ModDataComponents.init();
            
        //ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ModCommonConfig.SPEC, MOD_ID + "-common.toml");

        DLNetworkManager.registerPackets(MOD_ID, List.of(
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
            LinkerModePacket.class
        ), List.of(
            TrafficSignTextureResetPacket.class,
            TrafficSignWorkbenchUpdateClientPacket.class
        ));

        CrossPlatform.registerConfig();
    }
}
