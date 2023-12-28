package de.mrjulsen.trafficcraft.network.packets.stc;

import java.util.function.Supplier;

import de.mrjulsen.mcdragonlib.network.IPacketBase;
import de.mrjulsen.mcdragonlib.network.NetworkManagerBase;
import de.mrjulsen.trafficcraft.client.ClientWrapper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public class TrafficSignTextureResetPacket implements IPacketBase<TrafficSignTextureResetPacket> {
    public String id;

    public TrafficSignTextureResetPacket() {}

    public TrafficSignTextureResetPacket(String id) {
        this.id = id;
    }

    @Override
    public void encode(TrafficSignTextureResetPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.id);
    }

    @Override
    public TrafficSignTextureResetPacket decode(FriendlyByteBuf buffer) {
        String id = buffer.readUtf();

        return new TrafficSignTextureResetPacket(id);
    }

    @Override
    public void handle(TrafficSignTextureResetPacket packet, Supplier<NetworkEvent.Context> context) {
        NetworkManagerBase.handlePacket(packet, context, () -> {
            NetworkManagerBase.executeOnClient(() -> {
                ClientWrapper.handleTrafficSignTextureResetPacket(packet, context);
            });
        });
    }

    @Override
    public NetworkDirection getDirection() {
        return NetworkDirection.PLAY_TO_CLIENT;
    }
}
