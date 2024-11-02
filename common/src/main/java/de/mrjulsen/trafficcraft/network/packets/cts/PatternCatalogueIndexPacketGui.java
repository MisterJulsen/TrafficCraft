package de.mrjulsen.trafficcraft.network.packets.cts;

import java.util.function.Supplier;

import de.mrjulsen.mcdragonlib.net.BaseNetworkPacket;
import de.mrjulsen.trafficcraft.client.screen.menu.TrafficSignWorkbenchMenu;
import de.mrjulsen.trafficcraft.item.PatternCatalogueItem;
import dev.architectury.networking.NetworkManager.PacketContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class PatternCatalogueIndexPacketGui extends BaseNetworkPacket<PatternCatalogueIndexPacketGui> {

    private int index;

    public PatternCatalogueIndexPacketGui() {}

    public PatternCatalogueIndexPacketGui(int index) {
        this.index = index;
    }

    @Override
    public void encode(PatternCatalogueIndexPacketGui packet, RegistryFriendlyByteBuf buffer) {
        buffer.writeInt(packet.index);
    }

    @Override
    public PatternCatalogueIndexPacketGui decode(RegistryFriendlyByteBuf buffer) {
        int index = buffer.readInt();

        return new PatternCatalogueIndexPacketGui(index);
    }
    
    @Override
    public void handle(PatternCatalogueIndexPacketGui packet, Supplier<PacketContext> contextSupplier) {
        contextSupplier.get().queue(() -> {
            ServerPlayer sender = (ServerPlayer)contextSupplier.get().getPlayer();
            if (sender.containerMenu instanceof TrafficSignWorkbenchMenu menu) {
                final ItemStack stack = menu.patternSlot.getItem();
                if (!(stack.getItem() instanceof PatternCatalogueItem item))
                    return;

                item.setSelectedIndex(stack, packet.index);
                menu.patternSlot.set(stack);
                menu.patternSlot.setChanged();
                menu.broadcastChanges();     
            }
        });
    }
}
