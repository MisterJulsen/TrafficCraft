package de.mrjulsen.trafficcraft.network;

import java.util.Collection;
import java.util.List;

import de.mrjulsen.mcdragonlib.network.IPacketBase;
import de.mrjulsen.mcdragonlib.network.NetworkManagerBase;
import de.mrjulsen.trafficcraft.ModMain;
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

public class NetworkManager extends NetworkManagerBase<NetworkManager> {

    private static NetworkManager instance;

    public NetworkManager(String modid, String channelName, String protocolVersion) {
        super(modid, channelName, protocolVersion);
    }

    public static void create() {
        instance = NetworkManagerBase.create(NetworkManager.class, ModMain.MOD_ID, "trafficcraft_network", "2");
    }

    public static NetworkManager getInstance() {
        return instance;
    }

    @Override
    public Collection<Class<? extends IPacketBase<?>>> packets() {
        return List.of(
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
        );
    }
    
}
