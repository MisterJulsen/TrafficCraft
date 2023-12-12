package de.mrjulsen.trafficcraft.network.packets.cts;

import java.util.function.Supplier;

import de.mrjulsen.mcdragonlib.network.IPacketBase;
import de.mrjulsen.mcdragonlib.network.NetworkManagerBase;
import de.mrjulsen.trafficcraft.data.TrafficSignData;
import de.mrjulsen.trafficcraft.item.CreativePatternCatalogueItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public class CreativePatternCataloguePacket implements IPacketBase<CreativePatternCataloguePacket> {
    
    private TrafficSignData data;

    public CreativePatternCataloguePacket() {}

    public CreativePatternCataloguePacket(TrafficSignData data) {
        this.data = data;
    }

    @Override
    public void encode(CreativePatternCataloguePacket packet, FriendlyByteBuf buffer) {
        packet.data.toBytes(buffer);
    }

    @Override
    public CreativePatternCataloguePacket decode(FriendlyByteBuf buffer) {
        TrafficSignData data = TrafficSignData.fromBytes(buffer);

        return new CreativePatternCataloguePacket(data);
    }

    @Override
    public void handle(CreativePatternCataloguePacket packet, Supplier<NetworkEvent.Context> context) {
        NetworkManagerBase.handlePacket(packet, context, () -> {
            ServerPlayer sender = context.get().getSender();
            if (sender.getMainHandItem().getItem() instanceof CreativePatternCatalogueItem) {
                CreativePatternCatalogueItem.setCustomImage(sender.getMainHandItem(), packet.data);
                CreativePatternCatalogueItem.setSelectedIndex(sender.getMainHandItem(), -1);
            } else if (sender.getOffhandItem().getItem() instanceof CreativePatternCatalogueItem) { 
                CreativePatternCatalogueItem.setCustomImage(sender.getOffhandItem(), packet.data);
                CreativePatternCatalogueItem.setSelectedIndex(sender.getMainHandItem(), -1);
            }
        });
    }

    @Override
    public NetworkDirection getDirection() {
        return NetworkDirection.PLAY_TO_SERVER;
    }
}
