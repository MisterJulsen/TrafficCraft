package de.mrjulsen.trafficcraft.network.packets.cts;

import java.util.function.Supplier;

import de.mrjulsen.mcdragonlib.net.IPacketBase;
import de.mrjulsen.trafficcraft.item.CreativePatternCatalogueItem;
import de.mrjulsen.trafficcraft.item.PatternCatalogueItem;
import dev.architectury.networking.NetworkManager.PacketContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

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
    public void handle(PatternCatalogueIndexPacket packet, Supplier<PacketContext> contextSupplier) {
        contextSupplier.get().queue(() -> {
            Player sender = contextSupplier.get().getPlayer();
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
            sender.getInventory().setChanged();
        });
    }
}
