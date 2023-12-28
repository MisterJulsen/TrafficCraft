package de.mrjulsen.trafficcraft.network.packets.cts;

import java.util.function.Supplier;

import de.mrjulsen.mcdragonlib.network.IPacketBase;
import de.mrjulsen.mcdragonlib.network.NetworkManagerBase;
import de.mrjulsen.trafficcraft.client.screen.menu.TrafficSignWorkbenchMenu;
import de.mrjulsen.trafficcraft.item.PatternCatalogueItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public class PatternCatalogueIndexPacketGui implements IPacketBase<PatternCatalogueIndexPacketGui> {

    private int index;

    public PatternCatalogueIndexPacketGui() {}

    public PatternCatalogueIndexPacketGui(int index) {
        this.index = index;
    }

    @Override
    public void encode(PatternCatalogueIndexPacketGui packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.index);
    }

    @Override
    public PatternCatalogueIndexPacketGui decode(FriendlyByteBuf buffer) {
        int index = buffer.readInt();

        return new PatternCatalogueIndexPacketGui(index);
    }

    @Override
    public void handle(PatternCatalogueIndexPacketGui packet, Supplier<NetworkEvent.Context> context) {
        NetworkManagerBase.handlePacket(packet, context, () -> {
            ServerPlayer sender = context.get().getSender();
            if (sender.containerMenu instanceof TrafficSignWorkbenchMenu menu) {
                final ItemStack stack = menu.patternSlot.getItem();
                if (!(stack.getItem() instanceof PatternCatalogueItem))
                    return;

                PatternCatalogueItem.setSelectedIndex(stack, packet.index);
                menu.patternSlot.set(stack);
                menu.patternSlot.setChanged();
                menu.broadcastChanges();     
            }
        });
    }

    @Override
    public NetworkDirection getDirection() {
        return NetworkDirection.PLAY_TO_SERVER;
    }
}
