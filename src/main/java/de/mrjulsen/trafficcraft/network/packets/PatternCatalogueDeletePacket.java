package de.mrjulsen.trafficcraft.network.packets;

import java.util.function.Supplier;

import de.mrjulsen.trafficcraft.item.PatternCatalogueItem;
import de.mrjulsen.trafficcraft.network.NetworkManager;
import de.mrjulsen.trafficcraft.screen.menu.TrafficSignWorkbenchMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public class PatternCatalogueDeletePacket
{
    private int index;

    public PatternCatalogueDeletePacket(int index) {
        this.index = index;
    }

    public static void encode(PatternCatalogueDeletePacket packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.index);
    }

    public static PatternCatalogueDeletePacket decode(FriendlyByteBuf buffer) {
        int index = buffer.readInt();

        return new PatternCatalogueDeletePacket(index);
    }

    public static void handle(PatternCatalogueDeletePacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer sender = context.get().getSender();
            if (sender.containerMenu instanceof TrafficSignWorkbenchMenu menu) {
                final ItemStack stack = menu.patternSlot.getItem();
                if (!(stack.getItem() instanceof PatternCatalogueItem))
                    return;

                PatternCatalogueItem.removePatternAt(stack, packet.index);
                menu.patternSlot.set(stack);
                menu.patternSlot.setChanged();
                menu.broadcastChanges();
                
                NetworkManager.MOD_CHANNEL.sendTo(new TrafficSignWorkbenchUpdateClientPacket(), context.get().getSender().connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
            }
        });
        context.get().setPacketHandled(true);
    }
}