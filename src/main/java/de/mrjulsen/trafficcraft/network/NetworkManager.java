package de.mrjulsen.trafficcraft.network;

import de.mrjulsen.trafficcraft.ModMain;
import de.mrjulsen.trafficcraft.network.packets.PaintBrushPacket;
import de.mrjulsen.trafficcraft.network.packets.SignPacket;
import de.mrjulsen.trafficcraft.network.packets.TrafficLightControllerPacket;
import de.mrjulsen.trafficcraft.network.packets.TrafficLightSchedulePacket;
import de.mrjulsen.trafficcraft.network.packets.TrafficLightPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkManager {
    public static final String PROTOCOL_VERSION = String.valueOf(1);

    public static final SimpleChannel MOD_CHANNEL = NetworkRegistry.ChannelBuilder.named(new ResourceLocation(ModMain.MOD_ID, "trafficcraft_channel")).networkProtocolVersion(() -> PROTOCOL_VERSION).clientAcceptedVersions(PROTOCOL_VERSION::equals).serverAcceptedVersions(PROTOCOL_VERSION::equals).simpleChannel();
    
    public static void registerNetworkPackets()
    {
        MOD_CHANNEL.messageBuilder(SignPacket.class, 0).encoder(SignPacket::encode).decoder(SignPacket::decode).consumer(SignPacket::handle).add();
        MOD_CHANNEL.messageBuilder(TrafficLightPacket.class, 1).encoder(TrafficLightPacket::encode).decoder(TrafficLightPacket::decode).consumer(TrafficLightPacket::handle).add();
        MOD_CHANNEL.messageBuilder(TrafficLightSchedulePacket.class, 2).encoder(TrafficLightSchedulePacket::encode).decoder(TrafficLightSchedulePacket::decode).consumer(TrafficLightSchedulePacket::handle).add();
        MOD_CHANNEL.messageBuilder(TrafficLightControllerPacket.class, 3).encoder(TrafficLightControllerPacket::encode).decoder(TrafficLightControllerPacket::decode).consumer(TrafficLightControllerPacket::handle).add();
        MOD_CHANNEL.messageBuilder(PaintBrushPacket.class, 4).encoder(PaintBrushPacket::encode).decoder(PaintBrushPacket::decode).consumer(PaintBrushPacket::handle).add();
    }

    public static SimpleChannel getPlayChannel()
    {
        return MOD_CHANNEL;
    }
}
