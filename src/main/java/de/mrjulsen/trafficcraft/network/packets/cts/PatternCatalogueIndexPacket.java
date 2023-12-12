package de.mrjulsen.trafficcraft.network.packets.cts;

import java.util.function.Supplier;

import de.mrjulsen.mcdragonlib.network.IPacketBase;
import de.mrjulsen.mcdragonlib.network.NetworkManagerBase;
import de.mrjulsen.trafficcraft.item.CreativePatternCatalogueItem;
import de.mrjulsen.trafficcraft.item.PatternCatalogueItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public class PatternCatalogueIndexPacket implements IPacketBase<PatternCatalogueIndexPacket> {

    private int index;

    public PatternCatalogueIndexPacket() {}

    public PatternCatalogueIndexPacket(int index) {
        this.index = index;
    }

    @Override
    public void encode(PatternCatalogueIndexPacket packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.index);
    }

    @Override
    public PatternCatalogueIndexPacket decode(FriendlyByteBuf buffer) {
        int index = buffer.readInt();

        return new PatternCatalogueIndexPacket(index);
    }

    @Override
    public void handle(PatternCatalogueIndexPacket packet, Supplier<NetworkEvent.Context> context) {
        NetworkManagerBase.handlePacket(packet, context, () -> {
            ServerPlayer sender = context.get().getSender();
            if (sender.getMainHandItem().getItem() instanceof PatternCatalogueItem) {
                PatternCatalogueItem.setSelectedIndex(sender.getMainHandItem(), packet.index);
                if (sender.getMainHandItem().getItem() instanceof CreativePatternCatalogueItem) {
                    CreativePatternCatalogueItem.clearCustomImage(sender.getMainHandItem());
                }
            } else if (sender.getOffhandItem().getItem() instanceof PatternCatalogueItem) { 
                PatternCatalogueItem.setSelectedIndex(sender.getOffhandItem(), packet.index);
                if (sender.getOffhandItem().getItem() instanceof CreativePatternCatalogueItem) { 
                    CreativePatternCatalogueItem.clearCustomImage(sender.getOffhandItem());
                }
            }
        });
    }
    @Override
    public NetworkDirection getDirection() {
        return NetworkDirection.PLAY_TO_SERVER;
    }
}
