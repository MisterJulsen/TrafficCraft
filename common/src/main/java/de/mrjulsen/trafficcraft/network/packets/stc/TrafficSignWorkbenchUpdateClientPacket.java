package de.mrjulsen.trafficcraft.network.packets.stc;

import java.util.function.Supplier;

import de.mrjulsen.mcdragonlib.net.BaseNetworkPacket;
import de.mrjulsen.trafficcraft.client.ClientWrapper;
import dev.architectury.networking.NetworkManager.PacketContext;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.network.RegistryFriendlyByteBuf;

public class TrafficSignWorkbenchUpdateClientPacket extends BaseNetworkPacket<TrafficSignWorkbenchUpdateClientPacket> {

    public TrafficSignWorkbenchUpdateClientPacket() {}

    @Override
    public void encode(TrafficSignWorkbenchUpdateClientPacket packet, RegistryFriendlyByteBuf buffer) {
        
    }

    @Override
    public TrafficSignWorkbenchUpdateClientPacket decode(RegistryFriendlyByteBuf buffer) {
        return new TrafficSignWorkbenchUpdateClientPacket();
    }
    
    @Override
    public void handle(TrafficSignWorkbenchUpdateClientPacket packet, Supplier<PacketContext> contextSupplier) {
        contextSupplier.get().queue(() -> {
            EnvExecutor.runInEnv(Env.CLIENT, () -> () -> {
                ClientWrapper.handleTrafficSignWorkbenchUpdateClientPacket(packet);
            });
        });
    }
}
