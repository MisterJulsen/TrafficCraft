package de.mrjulsen.trafficcraft.network.packets;

import java.util.function.Supplier;

import de.mrjulsen.trafficcraft.item.PatternCatalogueItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class PatternCatalogueIndexPacket
{
    private int index;

    public PatternCatalogueIndexPacket(int index) {
        this.index = index;
    }

    public static void encode(PatternCatalogueIndexPacket packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.index);
    }

    public static PatternCatalogueIndexPacket decode(FriendlyByteBuf buffer) {
        int index = buffer.readInt();

        return new PatternCatalogueIndexPacket(index);
    }

    public static void handle(PatternCatalogueIndexPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer sender = context.get().getSender();
            if (sender.getMainHandItem().getItem() instanceof PatternCatalogueItem) {
                PatternCatalogueItem.setSelectedIndex(sender.getMainHandItem(), packet.index);
            } else if (sender.getOffhandItem().getItem() instanceof PatternCatalogueItem) { 
                PatternCatalogueItem.setSelectedIndex(sender.getOffhandItem(), packet.index);
            }
        });
        context.get().setPacketHandled(true);
    }
}
