package de.mrjulsen.trafficcraft.network.packets;

import java.util.function.Supplier;

import de.mrjulsen.trafficcraft.data.TrafficSignData;
import de.mrjulsen.trafficcraft.item.PatternCatalogueItem;
import de.mrjulsen.trafficcraft.screen.menu.TrafficSignWorkbenchMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

public class TrafficSignPatternPacket
{
    private TrafficSignData data;
    private int index;

    /**
     * @param data TrafficSign data.
     * @param index The index of the slot you want to replace or -1 to create a new pattern.
     */
    public TrafficSignPatternPacket(TrafficSignData data, int index) {
        this.index = index;
        this.data = data;
    }

    public static void encode(TrafficSignPatternPacket packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.index);
        packet.data.toBytes(buffer);
    }

    public static TrafficSignPatternPacket decode(FriendlyByteBuf buffer) {
        int index = buffer.readInt();
        TrafficSignData data = TrafficSignData.fromBytes(buffer);

        return new TrafficSignPatternPacket(data, index);
    }

    public static void handle(TrafficSignPatternPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer sender = context.get().getSender();
            if (sender.containerMenu instanceof TrafficSignWorkbenchMenu menu) {
                final ItemStack stack = menu.patternSlot.getItem();
                if (!(stack.getItem() instanceof PatternCatalogueItem))
                    return;

                if (packet.index >= 0) {
                    PatternCatalogueItem.replacePattern(stack, packet.data, packet.index);
                } else {
                    PatternCatalogueItem.setPattern(stack, packet.data);
                }
                menu.patternSlot.set(stack);
                menu.patternSlot.setChanged();
                menu.broadcastChanges();     
            }
        });
        context.get().setPacketHandled(true);
    }
}
