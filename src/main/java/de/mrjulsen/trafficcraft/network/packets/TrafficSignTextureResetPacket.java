package de.mrjulsen.trafficcraft.network.packets;

import java.util.UUID;
import java.util.function.Supplier;

import de.mrjulsen.trafficcraft.client.ClientWrapper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class TrafficSignTextureResetPacket {
    public final UUID id;

    public TrafficSignTextureResetPacket(UUID id) {
        this.id = id;
    }

    public static void encode(TrafficSignTextureResetPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUUID(packet.id);
    }

    public static TrafficSignTextureResetPacket decode(FriendlyByteBuf buffer) {
        UUID id = buffer.readUUID();

        return new TrafficSignTextureResetPacket(id);
    }

    public static void handle(TrafficSignTextureResetPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientWrapper.handleTrafficSignTextureResetPacket(packet, context));
        });
        context.get().setPacketHandled(true);
    }
}
