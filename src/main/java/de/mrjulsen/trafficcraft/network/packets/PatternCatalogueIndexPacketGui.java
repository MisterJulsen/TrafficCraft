package de.mrjulsen.trafficcraft.network.packets;

import java.util.function.Supplier;

import de.mrjulsen.trafficcraft.item.PatternCatalogueItem;
import de.mrjulsen.trafficcraft.screen.menu.TrafficSignWorkbenchMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

public class PatternCatalogueIndexPacketGui
{
    private int index;

    public PatternCatalogueIndexPacketGui(int index) {
        this.index = index;
    }

    public static void encode(PatternCatalogueIndexPacketGui packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.index);
    }

    public static PatternCatalogueIndexPacketGui decode(FriendlyByteBuf buffer) {
        int index = buffer.readInt();

        return new PatternCatalogueIndexPacketGui(index);
    }

    public static void handle(PatternCatalogueIndexPacketGui packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
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
        context.get().setPacketHandled(true);
    }
}
