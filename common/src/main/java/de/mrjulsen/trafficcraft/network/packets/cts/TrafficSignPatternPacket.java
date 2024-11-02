package de.mrjulsen.trafficcraft.network.packets.cts;

import java.util.function.Supplier;

import de.mrjulsen.mcdragonlib.net.BaseNetworkPacket;
import de.mrjulsen.mcdragonlib.net.DLNetworkManager;
import de.mrjulsen.mcdragonlib.util.DLUtils;
import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.client.screen.menu.TrafficSignWorkbenchMenu;
import de.mrjulsen.trafficcraft.data.NamedTrafficSignTextureReference;
import de.mrjulsen.trafficcraft.item.PatternCatalogueItem;
import de.mrjulsen.trafficcraft.network.packets.stc.TrafficSignWorkbenchUpdateClientPacket;
import dev.architectury.networking.NetworkManager.PacketContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class TrafficSignPatternPacket extends BaseNetworkPacket<TrafficSignPatternPacket> {
    
    private NamedTrafficSignTextureReference reference;
    private int index;

    public TrafficSignPatternPacket() {}

    /**
     * @param reference TrafficSign data.
     * @param index The index of the slot you want to replace or -1 to create a new pattern.
     */
    public TrafficSignPatternPacket(NamedTrafficSignTextureReference reference, int index) {
        this.index = index;
        this.reference = reference;
    }

    @Override
    public void encode(TrafficSignPatternPacket packet, RegistryFriendlyByteBuf buffer) {
        buffer.writeInt(packet.index);
        NamedTrafficSignTextureReference.toNetwork(buffer, packet.reference);
    }

    @Override
    public TrafficSignPatternPacket decode(RegistryFriendlyByteBuf buffer) {
        int index = buffer.readInt();
        NamedTrafficSignTextureReference reference = NamedTrafficSignTextureReference.fromNetwork(buffer);

        return new TrafficSignPatternPacket(reference, index);
    }
    
    @Override
    public void handle(TrafficSignPatternPacket packet, Supplier<PacketContext> contextSupplier) {
        contextSupplier.get().queue(() -> {
            ServerPlayer sender = (ServerPlayer)contextSupplier.get().getPlayer();
            if (sender.containerMenu instanceof TrafficSignWorkbenchMenu menu) {
                final ItemStack stack = menu.patternSlot.getItem();
                if (!(stack.getItem() instanceof PatternCatalogueItem item))
                    return;

                if (packet.index >= 0) {
                    item.replacePattern(stack, packet.reference, packet.index);
                } else {
                    item.setPattern(stack, packet.reference);
                }
                menu.patternSlot.set(stack);
                menu.patternSlot.setChanged();
                menu.broadcastChanges();

                DLUtils.giveAdvancement(sender, TrafficCraft.MOD_ID, "create_traffic_sign_pattern", "requirement");

                DLNetworkManager.sendToPlayer(sender, new TrafficSignWorkbenchUpdateClientPacket());
            }
        });
    }
}
