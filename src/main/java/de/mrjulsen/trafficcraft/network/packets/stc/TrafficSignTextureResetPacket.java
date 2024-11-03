package de.mrjulsen.trafficcraft.network.packets.stc;

import java.util.function.Supplier;

import de.mrjulsen.mcdragonlib.net.IPacketBase;
import de.mrjulsen.trafficcraft.client.ClientWrapper;
import dev.architectury.networking.NetworkManager.PacketContext;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.network.FriendlyByteBuf;

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
    public void handle(TrafficSignTextureResetPacket packet, Supplier<PacketContext> contextSupplier) {
        contextSupplier.get().queue(() -> {
            EnvExecutor.runInEnv(Env.CLIENT, () -> () -> {
                ClientWrapper.handleTrafficSignTextureResetPacket(packet, contextSupplier);
            });
        });
    }
}
