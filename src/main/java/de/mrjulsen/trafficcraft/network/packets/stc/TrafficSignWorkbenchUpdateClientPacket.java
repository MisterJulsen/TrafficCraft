package de.mrjulsen.trafficcraft.network.packets.stc;

import java.util.function.Supplier;

import de.mrjulsen.mcdragonlib.network.IPacketBase;
import de.mrjulsen.mcdragonlib.network.NetworkManagerBase;
import de.mrjulsen.trafficcraft.client.ClientWrapper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public class TrafficSignWorkbenchUpdateClientPacket implements IPacketBase<TrafficSignWorkbenchUpdateClientPacket> {

    public TrafficSignWorkbenchUpdateClientPacket() {}

    @Override
    public void encode(TrafficSignWorkbenchUpdateClientPacket packet, FriendlyByteBuf buffer) {
        
    }

    @Override
    public TrafficSignWorkbenchUpdateClientPacket decode(FriendlyByteBuf buffer) {
        return new TrafficSignWorkbenchUpdateClientPacket();
    }

    @Override
    public void handle(TrafficSignWorkbenchUpdateClientPacket packet, Supplier<NetworkEvent.Context> context) {
        NetworkManagerBase.handlePacket(packet, context, () -> {
            NetworkManagerBase.executeOnClient(() -> {
                ClientWrapper.handleTrafficSignWorkbenchUpdateClientPacket(packet, context);
            });
        });
    }

    @Override
    public NetworkDirection getDirection() {
        return NetworkDirection.PLAY_TO_CLIENT;
    }
}
