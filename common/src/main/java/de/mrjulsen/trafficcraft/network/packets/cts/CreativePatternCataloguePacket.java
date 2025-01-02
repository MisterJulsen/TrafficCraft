package de.mrjulsen.trafficcraft.network.packets.cts;

import java.util.function.Supplier;

import de.mrjulsen.mcdragonlib.net.IPacketBase;
import de.mrjulsen.trafficcraft.data.NamedTrafficSignTextureReference;
import de.mrjulsen.trafficcraft.item.CreativePatternCatalogueItem;
import dev.architectury.networking.NetworkManager.PacketContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class CreativePatternCataloguePacket implements IPacketBase<CreativePatternCataloguePacket> {
    
    private NamedTrafficSignTextureReference data;

    public CreativePatternCataloguePacket() {}

    public CreativePatternCataloguePacket(NamedTrafficSignTextureReference data) {
        this.data = data;
    }

    @Override
    public void encode(CreativePatternCataloguePacket packet, FriendlyByteBuf buffer) {
        buffer.writeNbt(packet.data.toNbt());
    }

    @Override
    public CreativePatternCataloguePacket decode(FriendlyByteBuf buffer) {
        return new CreativePatternCataloguePacket(NamedTrafficSignTextureReference.fromNbt(buffer.readNbt()));
    }

    @Override
    public void handle(CreativePatternCataloguePacket packet, Supplier<PacketContext> contextSupplier) {
        contextSupplier.get().queue(() -> {
            ServerPlayer sender = (ServerPlayer)contextSupplier.get().getPlayer();
            if (sender.getMainHandItem().getItem() instanceof CreativePatternCatalogueItem) {
                CreativePatternCatalogueItem.setCustomImage(sender.getMainHandItem(), packet.data);
                CreativePatternCatalogueItem.setSelectedIndex(sender.getMainHandItem(), -1);
            } else if (sender.getOffhandItem().getItem() instanceof CreativePatternCatalogueItem) { 
                CreativePatternCatalogueItem.setCustomImage(sender.getOffhandItem(), packet.data);
                CreativePatternCatalogueItem.setSelectedIndex(sender.getMainHandItem(), -1);
            }
            sender.getInventory().setChanged();
        });        
    }
}
