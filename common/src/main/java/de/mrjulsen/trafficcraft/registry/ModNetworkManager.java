package de.mrjulsen.trafficcraft.registry;

import de.mrjulsen.mcdragonlib.network.DLNetworkManager;
import de.mrjulsen.mcdragonlib.network.NetworkDirection;
import de.mrjulsen.mcdragonlib.network.NetworkPacketType;
import de.mrjulsen.mcdragonlib.util.DLUtils;
import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.network.packets.cts.*;
import de.mrjulsen.trafficcraft.network.packets.stc.TrafficSignTextureResetPacket;

public class ModNetworkManager {

    public static final DLNetworkManager NETWORK = new DLNetworkManager(DLUtils.resourceLocation(TrafficCraft.MOD_ID, "network"), "2");

    //public static final NetworkPacketType.SendAndReceive<NetworkDirection.C2S, GetTrafficSignTexturePacket.Request, GetTrafficSignTexturePacket.Response> GET_TRAFFIC_SIGN_TEXTURE = NETWORK.registerSendAndReceivePacket("get_traffic_sign_texture", NetworkDirection.C2S, GetTrafficSignTexturePacket::handle, GetTrafficSignTexturePacket.Request::new, GetTrafficSignTexturePacket.Response::new);
    public static final NetworkPacketType.SendAndReceive<NetworkDirection.C2S, GetTexturePacket.Request, GetTexturePacket.Response> GET_TEXTURE = NETWORK.registerSendAndReceivePacket("get_texture", NetworkDirection.C2S, GetTexturePacket::handle, GetTexturePacket.Request::new, GetTexturePacket.Response::new);
    public static final NetworkPacketType.SendAndReceive<NetworkDirection.C2S, PatternCatalogueDeletePacket.Request, PatternCatalogueDeletePacket.Response> DELETE_PATTERN_CATALOG_ENTRY = NETWORK.registerSendAndReceivePacket("delete_pattern_catalog_entry", NetworkDirection.C2S, PatternCatalogueDeletePacket::handle, PatternCatalogueDeletePacket.Request::new, PatternCatalogueDeletePacket.Response::new);
    public static final NetworkPacketType.SendAndReceive<NetworkDirection.C2S, TrafficSignPatternPacket.Request, TrafficSignPatternPacket.Response> UPDATE_TRAFFIC_SIGN_PATTERN = NETWORK.registerSendAndReceivePacket("update_traffic_sign_pattern", NetworkDirection.C2S, TrafficSignPatternPacket::handle, TrafficSignPatternPacket.Request::new, TrafficSignPatternPacket.Response::new);
    //public static final NetworkPacketType.SendAndReceive<NetworkDirection.C2S, CreateNewTrafficSignTexturePacket.Request, CreateNewTrafficSignTexturePacket.Response> CREATE_NEW_TRAFFIC_SIGN_TEXTURE = NETWORK.registerSendAndReceivePacket("create_new_traffic_sign_texture", NetworkDirection.C2S, CreateNewTrafficSignTexturePacket::handle, CreateNewTrafficSignTexturePacket.Request::new, CreateNewTrafficSignTexturePacket.Response::new);
    public static final NetworkPacketType.SendAndReceive<NetworkDirection.C2S, ColorPaletteItemPacket.Request, ColorPaletteItemPacket.Response> UPDATE_COLOR_PALETTE_ITEM = NETWORK.registerSendAndReceivePacket("update_color_palette_item", NetworkDirection.C2S, ColorPaletteItemPacket::handle, ColorPaletteItemPacket.Request::new, ColorPaletteItemPacket.Response::new);
    public static final NetworkPacketType.SendAndReceive<NetworkDirection.C2S, PatternCatalogueIndexPacketGui.Request, PatternCatalogueIndexPacketGui.Response> UPDATE_PATTERN_CATALOG_INDEX_IN_GUI = NETWORK.registerSendAndReceivePacket("update_pattern_catalog_index_in_gui", NetworkDirection.C2S, PatternCatalogueIndexPacketGui::handle, PatternCatalogueIndexPacketGui.Request::new, PatternCatalogueIndexPacketGui.Response::new);
    public static final NetworkPacketType.SendAndReceive<NetworkDirection.C2S, SaveTexturePacket.Request, SaveTexturePacket.Response> SAVE_TEXTURE = NETWORK.registerSendAndReceivePacket("save_texture", NetworkDirection.C2S, SaveTexturePacket::handle, SaveTexturePacket.Request::new, SaveTexturePacket.Response::new);

    public static final NetworkPacketType.Send<NetworkDirection.C2S, CreativePatternCataloguePacket> UPDATE_CREATIVE_PATTERN_CATALOG_ITEM = NETWORK.registerSendOnlyPacket("update_creative_pattern_catalog_item", NetworkDirection.C2S, CreativePatternCataloguePacket::handle, CreativePatternCataloguePacket::new);
    public static final NetworkPacketType.Send<NetworkDirection.C2S, LinkerModePacket> UPDATE_LINK_MODE = NETWORK.registerSendOnlyPacket("update_link_mode", NetworkDirection.C2S, LinkerModePacket::handle, LinkerModePacket::new);
    public static final NetworkPacketType.Send<NetworkDirection.C2S, PaintBrushPacket> UPDATE_PAINT_BRUSH = NETWORK.registerSendOnlyPacket("update_paint_brush", NetworkDirection.C2S, PaintBrushPacket::handle, PaintBrushPacket::new);
    public static final NetworkPacketType.Send<NetworkDirection.C2S, PatternCatalogueIndexPacket> UPDATE_PATTERN_CATALOG_INDEX = NETWORK.registerSendOnlyPacket("update_pattern_catalog_index", NetworkDirection.C2S, PatternCatalogueIndexPacket::handle, PatternCatalogueIndexPacket::new);
    public static final NetworkPacketType.Send<NetworkDirection.C2S, RoadBuilderBuildRoadPacket> ROAD_BUILDER_BUILD_ROAD = NETWORK.registerSendOnlyPacket("road_builder_build_road", NetworkDirection.C2S, RoadBuilderBuildRoadPacket::handle, RoadBuilderBuildRoadPacket::new);
    public static final NetworkPacketType.Send<NetworkDirection.C2S, RoadBuilderDataPacket> UPDATE_ROAD_BUILDER = NETWORK.registerSendOnlyPacket("update_road_builder", NetworkDirection.C2S, RoadBuilderDataPacket::handle, RoadBuilderDataPacket::new);
    public static final NetworkPacketType.Send<NetworkDirection.C2S, RoadBuilderResetPacket> RESET_ROAD_BUILDER = NETWORK.registerSendOnlyPacket("reset_road_builder", NetworkDirection.C2S, RoadBuilderResetPacket::handle, RoadBuilderResetPacket::new);
    public static final NetworkPacketType.Send<NetworkDirection.C2S, StreetLampConfigPacket> UPDATE_STREET_LAMP_CONFIG_CARD = NETWORK.registerSendOnlyPacket("update_street_lamp_config_card", NetworkDirection.C2S, StreetLampConfigPacket::handle, StreetLampConfigPacket::new);
    public static final NetworkPacketType.Send<NetworkDirection.C2S, TownSignPacket> UPDATE_TOWN_SIGN = NETWORK.registerSendOnlyPacket("update_town_sign", NetworkDirection.C2S, TownSignPacket::handle, TownSignPacket::new);
    public static final NetworkPacketType.Send<NetworkDirection.C2S, TrafficLightControllerPacket> UPDATE_TRAFFIC_LIGHT_CONTROLLER = NETWORK.registerSendOnlyPacket("update_traffic_light_controller", NetworkDirection.C2S, TrafficLightControllerPacket::handle, TrafficLightControllerPacket::new);
    public static final NetworkPacketType.Send<NetworkDirection.C2S, TrafficLightPacket> UPDATE_TRAFFIC_LIGHT_PACKET = NETWORK.registerSendOnlyPacket("update_traffic_light_packet", NetworkDirection.C2S, TrafficLightPacket::handle, TrafficLightPacket::new);
    public static final NetworkPacketType.Send<NetworkDirection.C2S, TrafficLightSchedulePacket> UPDATE_TRAFFIC_LIGHT_SCHEDULE = NETWORK.registerSendOnlyPacket("update_traffic_light_schedule", NetworkDirection.C2S, TrafficLightSchedulePacket::handle, TrafficLightSchedulePacket::new);
    public static final NetworkPacketType.Send<NetworkDirection.C2S, WritableSignPacket> UPDATE_WRITABLE_SIGN = NETWORK.registerSendOnlyPacket("update_writable_sign", NetworkDirection.C2S, WritableSignPacket::handle, WritableSignPacket::new);
    public static final NetworkPacketType.Send<NetworkDirection.C2S, UpdateTrafficSignShapePacket> UPDATE_SIGN_SAPE = NETWORK.registerSendOnlyPacket("update_sign_shape", NetworkDirection.C2S, UpdateTrafficSignShapePacket::handle, UpdateTrafficSignShapePacket::new);

    
    
    public static final NetworkPacketType.Send<NetworkDirection.S2C, TrafficSignTextureResetPacket> RESET_TRAFFIC_SIGN_TEXTURE = NETWORK.registerSendOnlyPacket("reset_traffic_sign_texture", NetworkDirection.S2C, TrafficSignTextureResetPacket::handle, TrafficSignTextureResetPacket::new);

    

    public static void init() {}
}
