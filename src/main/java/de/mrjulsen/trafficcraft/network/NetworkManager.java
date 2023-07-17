package de.mrjulsen.trafficcraft.network;

import de.mrjulsen.trafficcraft.ModMain;
import de.mrjulsen.trafficcraft.network.packets.PaintBrushPacket;
import de.mrjulsen.trafficcraft.network.packets.SignPacket;
import de.mrjulsen.trafficcraft.network.packets.StreetLampConfigPacket;
import de.mrjulsen.trafficcraft.network.packets.TownSignPacket;
import de.mrjulsen.trafficcraft.network.packets.TrafficLightControllerPacket;
import de.mrjulsen.trafficcraft.network.packets.TrafficLightSchedulePacket;
import de.mrjulsen.trafficcraft.network.packets.WritableSignPacket;
import de.mrjulsen.trafficcraft.network.packets.TrafficLightPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.network.simple.SimpleChannel.MessageBuilder;

public class NetworkManager {
    public static final String PROTOCOL_VERSION = String.valueOf(1);
    private static int currentId = 0;

    public static final SimpleChannel MOD_CHANNEL = NetworkRegistry.ChannelBuilder.named(new ResourceLocation(ModMain.MOD_ID, "trafficcraft_channel")).networkProtocolVersion(() -> PROTOCOL_VERSION).clientAcceptedVersions(PROTOCOL_VERSION::equals).serverAcceptedVersions(PROTOCOL_VERSION::equals).simpleChannel();
    
    public static void registerNetworkPackets()
    {
        register(SignPacket.class).encoder(SignPacket::encode).decoder(SignPacket::decode).consumer(SignPacket::handle).add();
        register(TrafficLightPacket.class).encoder(TrafficLightPacket::encode).decoder(TrafficLightPacket::decode).consumer(TrafficLightPacket::handle).add();
        register(TrafficLightSchedulePacket.class).encoder(TrafficLightSchedulePacket::encode).decoder(TrafficLightSchedulePacket::decode).consumer(TrafficLightSchedulePacket::handle).add();
        register(TrafficLightControllerPacket.class).encoder(TrafficLightControllerPacket::encode).decoder(TrafficLightControllerPacket::decode).consumer(TrafficLightControllerPacket::handle).add();
        register(PaintBrushPacket.class).encoder(PaintBrushPacket::encode).decoder(PaintBrushPacket::decode).consumer(PaintBrushPacket::handle).add();
        register(StreetLampConfigPacket.class).encoder(StreetLampConfigPacket::encode).decoder(StreetLampConfigPacket::decode).consumer(StreetLampConfigPacket::handle).add();
        register(WritableSignPacket.class).encoder(WritableSignPacket::encode).decoder(WritableSignPacket::decode).consumer(WritableSignPacket::handle).add();
        register(TownSignPacket.class).encoder(TownSignPacket::encode).decoder(TownSignPacket::decode).consumer(TownSignPacket::handle).add();
    }

    public static SimpleChannel getPlayChannel()
    {
        return MOD_CHANNEL;
    }

    private static <T> MessageBuilder<T> register(Class<T> clazz) {
        MessageBuilder<T> mb = MOD_CHANNEL.messageBuilder(clazz, currentId);
        currentId++;
        return mb;

    }
}
