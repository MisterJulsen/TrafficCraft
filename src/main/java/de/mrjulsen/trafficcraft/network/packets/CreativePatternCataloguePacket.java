package de.mrjulsen.trafficcraft.network.packets;

import java.util.function.Supplier;

import de.mrjulsen.trafficcraft.data.TrafficSignData;
import de.mrjulsen.trafficcraft.item.CreativePatternCatalogueItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class CreativePatternCataloguePacket
{
    private TrafficSignData data;

    public CreativePatternCataloguePacket(TrafficSignData data) {
        this.data = data;
    }

    public static void encode(CreativePatternCataloguePacket packet, FriendlyByteBuf buffer) {
        packet.data.toBytes(buffer);
    }

    public static CreativePatternCataloguePacket decode(FriendlyByteBuf buffer) {
        TrafficSignData data = TrafficSignData.fromBytes(buffer);

        return new CreativePatternCataloguePacket(data);
    }

    public static void handle(CreativePatternCataloguePacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer sender = context.get().getSender();
            if (sender.getMainHandItem().getItem() instanceof CreativePatternCatalogueItem) {
                CreativePatternCatalogueItem.setCustomImage(sender.getMainHandItem(), packet.data);
                CreativePatternCatalogueItem.setSelectedIndex(sender.getMainHandItem(), -1);
            } else if (sender.getOffhandItem().getItem() instanceof CreativePatternCatalogueItem) { 
                CreativePatternCatalogueItem.setCustomImage(sender.getOffhandItem(), packet.data);
                CreativePatternCatalogueItem.setSelectedIndex(sender.getMainHandItem(), -1);
            }
        });
        context.get().setPacketHandled(true);
    }
}
