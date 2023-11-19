package de.mrjulsen.trafficcraft.network.packets;

import java.util.function.Supplier;

import de.mrjulsen.trafficcraft.item.RoadConstructionTool;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class RoadBuilderResetPacket {
    
    public RoadBuilderResetPacket() {
    }

    public static void encode(RoadBuilderResetPacket packet, FriendlyByteBuf buffer) {
    }

    public static RoadBuilderResetPacket decode(FriendlyByteBuf buffer) {
        return new RoadBuilderResetPacket();
    }

    public static void handle(RoadBuilderResetPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer sender = context.get().getSender();

            if (sender.getMainHandItem().getItem() instanceof RoadConstructionTool) {
                RoadConstructionTool.reset(sender.getMainHandItem());
            } else if (sender.getOffhandItem().getItem() instanceof RoadConstructionTool) {
                RoadConstructionTool.reset(sender.getOffhandItem());
            }
        });
        context.get().setPacketHandled(true);
    }
}
