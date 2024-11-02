package de.mrjulsen.trafficcraft.network.packets.cts;

import java.util.function.Supplier;

import de.mrjulsen.mcdragonlib.net.BaseNetworkPacket;
import de.mrjulsen.trafficcraft.data.NamedTrafficSignTextureReference;
import de.mrjulsen.trafficcraft.item.CreativePatternCatalogueItem;
import dev.architectury.networking.NetworkManager.PacketContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class CreativePatternCataloguePacket extends BaseNetworkPacket<CreativePatternCataloguePacket> {
    
    private NamedTrafficSignTextureReference data;

    public CreativePatternCataloguePacket() {}

    public CreativePatternCataloguePacket(NamedTrafficSignTextureReference data) {
        this.data = data;
    }

    @Override
    public void encode(CreativePatternCataloguePacket packet, RegistryFriendlyByteBuf buffer) {
        NamedTrafficSignTextureReference.toNetwork(buffer, packet.data);
    }

    @Override
    public CreativePatternCataloguePacket decode(RegistryFriendlyByteBuf buffer) {
        return new CreativePatternCataloguePacket(NamedTrafficSignTextureReference.fromNetwork(buffer));
    }

    @Override
    public void handle(CreativePatternCataloguePacket packet, Supplier<PacketContext> contextSupplier) {
        contextSupplier.get().queue(() -> {
            ServerPlayer sender = (ServerPlayer)contextSupplier.get().getPlayer();
            if (sender.getMainHandItem().getItem() instanceof CreativePatternCatalogueItem item) {
                item.setCustomImage(sender.getMainHandItem(), packet.data);
                item.setSelectedIndex(sender.getMainHandItem(), -1);
            } else if (sender.getOffhandItem().getItem() instanceof CreativePatternCatalogueItem item) { 
                item.setCustomImage(sender.getOffhandItem(), packet.data);
                item.setSelectedIndex(sender.getMainHandItem(), -1);
            }
            sender.getInventory().setChanged();
        });        
    }
}
