package de.mrjulsen.trafficcraft.network.packets.cts;

import java.util.function.Supplier;

import de.mrjulsen.mcdragonlib.net.BaseNetworkPacket;
import de.mrjulsen.trafficcraft.item.CreativePatternCatalogueItem;
import de.mrjulsen.trafficcraft.item.PatternCatalogueItem;
import dev.architectury.networking.NetworkManager.PacketContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class PatternCatalogueIndexPacket extends BaseNetworkPacket<PatternCatalogueIndexPacket> {

    private int index;

    public PatternCatalogueIndexPacket() {}

    public PatternCatalogueIndexPacket(int index) {
        this.index = index;
    }

    @Override
    public void encode(PatternCatalogueIndexPacket packet, RegistryFriendlyByteBuf buffer) {
        buffer.writeInt(packet.index);
    }

    @Override
    public PatternCatalogueIndexPacket decode(RegistryFriendlyByteBuf buffer) {
        int index = buffer.readInt();

        return new PatternCatalogueIndexPacket(index);
    }
    
    @Override
    public void handle(PatternCatalogueIndexPacket packet, Supplier<PacketContext> contextSupplier) {
        contextSupplier.get().queue(() -> {
            ServerPlayer sender = (ServerPlayer)contextSupplier.get().getPlayer();
            if (sender.getMainHandItem().getItem() instanceof PatternCatalogueItem item) {
                item.setSelectedIndex(sender.getMainHandItem(), packet.index);
                if (sender.getMainHandItem().getItem() instanceof CreativePatternCatalogueItem creativeItem) {
                    creativeItem.clearCustomImage(sender.getMainHandItem());
                }
            } else if (sender.getOffhandItem().getItem() instanceof PatternCatalogueItem item) { 
                item.setSelectedIndex(sender.getOffhandItem(), packet.index);
                if (sender.getOffhandItem().getItem() instanceof CreativePatternCatalogueItem creativeItem) { 
                    creativeItem.clearCustomImage(sender.getOffhandItem());
                }
            }
            sender.getInventory().setChanged();
        });
    }
}
