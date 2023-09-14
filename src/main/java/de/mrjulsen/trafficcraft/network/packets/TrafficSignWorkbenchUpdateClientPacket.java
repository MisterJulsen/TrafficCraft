package de.mrjulsen.trafficcraft.network.packets;

import java.util.function.Supplier;

import de.mrjulsen.trafficcraft.client.ClientWrapper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class TrafficSignWorkbenchUpdateClientPacket {

    public static void encode(TrafficSignWorkbenchUpdateClientPacket packet, FriendlyByteBuf buffer) {
        
    }

    public static TrafficSignWorkbenchUpdateClientPacket decode(FriendlyByteBuf buffer) {
        return new TrafficSignWorkbenchUpdateClientPacket();
    }

    public static void handle(TrafficSignWorkbenchUpdateClientPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() ->
        {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientWrapper.handleTrafficSignWorkbenchUpdateClientPacket(packet, context));
        });
        
        context.get().setPacketHandled(true); 
    }
}
